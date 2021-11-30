/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.compiler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtils {

  /** Logger. */
  private static final Log log = LogFactory.getLog(GraphUtils.class);

  /**
   * Builds a graph of all the resources that are created with the transformation rules and determines which ones are
   * unique instances and which ones are created per patient.
   *
   * @param doc The rules document.
   * @return The results of the graph computation.
   */
  public static Results buildGraph(Document doc) {
    Results res = new Results();
    /*
     * We need to decide if an instance of a resource is created for every patient (i.e., for every
     * row in the data) or just once. This depends on the rules. If all the rules that refer to a
     * resource (with a specific id) never reference any values in the form, then a single resource
     * is created. If the rules have references to other resources then the decision depends on the
     * other resources.
     *
     * This means that we need to create a graph to:
     *   - Check that there are no cycles and throw an error if one is detected
     *   - Traverse the graph and look for all the nodes that are reference to check if all of them
     *   are independent of the rows.
     */
    final Graph<ResourceNode, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

    // First add all vertices
    for (Rule rule : doc.getRules()) {
      for (Resource r : rule.getResources()) {
        // Extract resource and create node
        ResourceNode rn = new ResourceNode(r);
        if (g.containsVertex(rn)) {
          rn = getVertex(g, rn.type, rn.id);
        } else {
          g.addVertex(rn);
        }
        // We might be merging various resources from multiple rules
        GrammarObject.DataReference ndr = r.referencesData();
        GrammarObject.DataReference odr = rn.referenceData;
        if (odr == null) {
          // If referenceData hasn't been set then just use this one
          rn.referenceData = ndr;
        } else {
          if (odr.equals(GrammarObject.DataReference.NO) && !ndr.equals(GrammarObject.DataReference.NO)) {
            rn.referenceData = ndr;
          } else if (odr.equals(GrammarObject.DataReference.RESOURCE)
            && ndr.equals(GrammarObject.DataReference.YES)) {
            rn.referenceData = ndr;
          }
        }
      }
    }

    // Then add all edges
    for (Rule rule : doc.getRules()) {
      for (Resource r : rule.getResources()) {
        ResourceNode rn = getVertex(g, r.getResourceType(), r.getResourceId());
        // Get references and create edges
        for (ReferenceValue rv : r.getReferences()) {
          ResourceNode tn = new ResourceNode(rv.getResourceType(), rv.getResourceId());
          if (g.containsVertex(tn)) {
            tn = getVertex(g, tn.type, tn.id);
          } else {
            res.diagnostics.add(new Diagnostic(new Range(new Position(0, 0), new Position(0, 0)),
              "Resource " + tn + " is missing. Did you create it in the rules?", DiagnosticSeverity.Error, "graph"));
            return res;
          }
          g.addEdge(rn, tn);
        }
      }
    }

    /*
     * At this point we have a directed graph and need to do the following:
     * 1. Check for cycles and throw an error if one is detected.
     * 2. Do an inverse topological sort so that we can do 3 and also so resources can be created in the correct order.
     * 3. Set the definitive value of referenceData for vertices.
     */
    HawickJamesSimpleCycles<ResourceNode, DefaultEdge> cd = new HawickJamesSimpleCycles<>(g);
    if (cd.countSimpleCycles() > 0) {
      String firstCycle = cd.findSimpleCycles().get(0).stream().map(v -> "(" + v.type + "<" + v.id + ">")
        .collect(Collectors.joining(", "));
      res.diagnostics.add(new Diagnostic(new Range(new Position(0, 0), new Position(0, 0)),
        "There is an illegal cycle in the rule definitions: " + firstCycle, DiagnosticSeverity.Error, "graph"));
      return res;
    }

    // Do inverse topological sort
    GraphIterator<ResourceNode, DefaultEdge> it = new TopologicalOrderIterator<>(g);
    while (it.hasNext()) {
      res.getSortedNodes().add(it.next());
    }
    Collections.reverse(res.getSortedNodes());

    // Set definitive value of referenceData for vertices
    for (ResourceNode rn : res.getSortedNodes()) {
      if (rn.referenceData == null) {
        res.diagnostics.add(new Diagnostic(new Range(new Position(0, 0), new Position(0, 0)),
          "Reference data in resource node " + rn + " is null.", DiagnosticSeverity.Error, "graph"));
        return res;
      }

      // If referenceData is REFERENCE then look for all references and update value
      if (rn.referenceData.equals(GrammarObject.DataReference.RESOURCE)) {
        int refCount = 0;
        boolean foundYes = false;
        it = new DepthFirstIterator<>(g, rn);
        while (it.hasNext()) {
          refCount++;
          if (it.next().referenceData.equals(GrammarObject.DataReference.YES)) {
            rn.referenceData = GrammarObject.DataReference.YES;
            foundYes = true;
            break;
          }
        }
        if (refCount == 0) {
          res.diagnostics.add(new Diagnostic(new Range(new Position(0, 0), new Position(0, 0)),
            "The rules created a malformed graph. Node " + rn.type + "<" + rn.id + "> has no references.",
            DiagnosticSeverity.Error, "graph"));
          return res;
        }
        if (!foundYes) {
          rn.referenceData = GrammarObject.DataReference.NO;
        }
      }
    }

    // Populate a set with the ids of unique resources - needed to create the right references to them
    for (ResourceNode rn : g.vertexSet()) {
      if (rn.referenceData.equals(GrammarObject.DataReference.NO)) {
        res.getUniqueIds().add(rn.toString());
      }
    }
    log.debug("Found the following unique resources: " + res.getUniqueIds());

    return res;
  }

  /**
   * Returns a vertex in the graph. Should only be called after checking the graph contains the vertex.
   *
   * @param g The graph.
   * @param resourceType The resource type of a vertex.
   * @param resourceId The resource id of a vertex.
   * @return The vertex in the graph.
   */
  private static ResourceNode getVertex(Graph<ResourceNode, DefaultEdge> g,
                                 String resourceType, String resourceId) {
    for (ResourceNode v : g.vertexSet()) {
      if(v.type.equals(resourceType) && v.id.equals(resourceId)) {
        return v;
      }
    }
    throw new RuntimeException("Resource node was null. This should never happen!");
  }

  /**
   * The results of computing the resource graph. Includes a list of nodes that are sorted in the order they should be
   * created, a set of ids of the resources that should be created only once (i.e., not per patient) and a list of
   * issues detected during the computation.
   */
  public static class Results {
    private final List<ResourceNode> sortedNodes = new ArrayList<>();
    private final Set<String> uniqueIds = new HashSet<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public List<ResourceNode> getSortedNodes() {
      return sortedNodes;
    }

    public Set<String> getUniqueIds() {
      return uniqueIds;
    }

    public List<Diagnostic> getDiagnostics() {
      return diagnostics;
    }
  }

  public static class ResourceNode {
    String type;
    String id;
    GrammarObject.DataReference referenceData;

    public ResourceNode(Resource r) {
      this.type = r.getResourceType();
      this.id = r.getResourceId();
    }

    public ResourceNode(String type, String id) {
      this.type = type;
      this.id = id;
    }

    public String getType() {
      return type;
    }

    public String getId() {
      return id;
    }

    public GrammarObject.DataReference getReferenceData() {
      return referenceData;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ResourceNode that = (ResourceNode) o;
      return type.equals(that.type) && id.equals(that.id);
    }

    public boolean equalsResource(Resource r) {
      return type.equals(r.getResourceType()) && id.equals(r.getResourceId());
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, id);
    }

    @Override
    public String toString() {
      return type + "<" + id + ">";
    }
  }
}
