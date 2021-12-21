package au.csiro.redmatch.exporter;

import au.csiro.redmatch.model.D3Graph;

import au.csiro.redmatch.model.D3Link;
import au.csiro.redmatch.model.D3Node;
import au.csiro.redmatch.util.Progress;
import au.csiro.redmatch.util.ProgressReporter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GraphExporterService {

  public void exportGraph(D3Graph d3Graph, File outputFolder, ProgressReporter progressReporter) throws IOException {
    try (FileWriter fw = new FileWriter(new File(outputFolder, "graph.html"))) {

      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Generating D3 file"));
      }

      StringBuilder sb = new StringBuilder();
      String pre = "<!DOCTYPE html>\n" +
        "<meta charset=\"utf-8\">\n" +
        "\n" +
        "<style>\n" +
        "div.tooltip {\n" +
        "    position: absolute;\n" +
        "    text-align: center;\n" +
        "    padding: 4px;\n" +
        "    font: 9px sans-serif;\n" +
        "    background: lightgray;\n" +
        "    border: 0px;\n" +
        "    border-radius: 9px;\n" +
        "    pointer-events: none;\n" +
        "}\n" +
        "</style>\n" +
        "\n" +
        "<script type=\"module\">\n" +
        "    import * as d3 from \"https://cdn.skypack.dev/d3@7\";\n";
      sb.append(pre);

      // Add links
      sb.append("const links = [\n");
      for (int i = 0; i < d3Graph.getLinks().size(); i++) {
        D3Link d3Link = d3Graph.getLinks().get(i);
        sb.append("{source: \"");
        sb.append(d3Link.getSource());
        sb.append("\", target: \"");
        sb.append(d3Link.getTarget());
        sb.append("\", type: \"");
        sb.append(d3Link.getType());
        sb.append("\"}");
        if (i < d3Graph.getLinks().size() - 1) {
          sb.append(",");
        }
        sb.append("\n");
      }
      sb.append("];\n\n");

      // Add types
      sb.append("const types = [\n");
      List<String> types = new ArrayList<>(d3Graph.getTypes());
      for (int i = 0; i < types.size(); i++) {
        String type = types.get(i);
        sb.append("\"");
        sb.append(type);
        sb.append("\"");
        if (i < types.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append("];\n\n");

      // Add nodes
      sb.append("const nodes = [\n");
      for (int i = 0; i < d3Graph.getNodes().size(); i++) {
        D3Node d3Node = d3Graph.getNodes().get(i);
        sb.append("{id: \"");
        sb.append(d3Node.getId());
        sb.append("\"}");
        if (i < d3Graph.getNodes().size() - 1) {
          sb.append(",");
        }
        sb.append("\n");
      }
      sb.append("];\n\n");

      String post = "    let colors = {};\n" +
        "\n" +
        "    const height = 600;\n" +
        "    const width = 800;\n" +
        "\n" +
        "    function linkArc(d) {\n" +
        "        const r = Math.hypot(d.target.x - d.source.x, d.target.y - d.source.y);\n" +
        "        return `\n" +
        "            M${d.source.x},${d.source.y}\n" +
        "            A${r},${r} 0 0,1 ${d.target.x},${d.target.y}\n" +
        "        `;\n" +
        "    }\n" +
        "\n" +
        "    const div = d3.select(\"body\").append(\"div\")\n" +
        "      .attr(\"class\", \"tooltip\")\n" +
        "      .style(\"opacity\", 0);\n" +
        "\n" +
        "    const color = d3.scaleOrdinal(types, d3.schemeCategory10);\n" +
        "\n" +
        "    const drag = simulation => {\n" +
        "  \n" +
        "        function dragstarted(event, d) {\n" +
        "            if (!event.active) simulation.alphaTarget(0.3).restart();\n" +
        "            d.fx = d.x;\n" +
        "            d.fy = d.y;\n" +
        "        }\n" +
        "  \n" +
        "        function dragged(event, d) {\n" +
        "            d.fx = event.x;\n" +
        "            d.fy = event.y;\n" +
        "        }\n" +
        "  \n" +
        "        function dragended(event, d) {\n" +
        "            if (!event.active) simulation.alphaTarget(0);\n" +
        "            d.fx = null;\n" +
        "            d.fy = null;\n" +
        "        }\n" +
        "  \n" +
        "          return d3.drag()\n" +
        "              .on(\"start\", dragstarted)\n" +
        "              .on(\"drag\", dragged)\n" +
        "              .on(\"end\", dragended);\n" +
        "    }\n" +
        "\n" +
        "    const simulation = d3.forceSimulation(nodes)\n" +
        "      .force(\"link\", d3.forceLink(links).id(d => d.id))\n" +
        "      .force(\"charge\", d3.forceManyBody().strength(-400))\n" +
        "      .force(\"x\", d3.forceX())\n" +
        "      .force(\"y\", d3.forceY());\n" +
        "\n" +
        "    const svg = d3.select(\"body\").append(\"svg\")\n" +
        "        .attr(\"viewBox\", [-width / 2, -height / 2, width, height])\n" +
        "        .style(\"font\", \"9px sans-serif\");\n" +
        "\n" +
        "    // Per-type markers, as they don't inherit styles.\n" +
        "    svg.append(\"defs\").selectAll(\"marker\")\n" +
        "        .data(types)\n" +
        "        .join(\"marker\")\n" +
        "        .attr(\"id\", d => `arrow-${d}`)\n" +
        "        .attr(\"viewBox\", \"0 -5 10 10\")\n" +
        "        .attr(\"refX\", 15)\n" +
        "        .attr(\"refY\", -0.5)\n" +
        "        .attr(\"markerWidth\", 6)\n" +
        "        .attr(\"markerHeight\", 6)\n" +
        "        .attr(\"orient\", \"auto\")\n" +
        "        .append(\"path\")\n" +
        "        .attr(\"fill\", color)\n" +
        "        .attr(\"d\", \"M0,-5L10,0L0,5\");\n" +
        "\n" +
        "    const link = svg.append(\"g\")\n" +
        "        .attr(\"fill\", \"none\")\n" +
        "        .attr(\"stroke-width\", 1.5)\n" +
        "        .selectAll(\"path\")\n" +
        "        .data(links)\n" +
        "        .join(\"path\")\n" +
        "        .attr(\"stroke\", d => color(d.type))\n" +
        "        .attr(\"marker-end\", d => `url(${new URL(`#arrow-${d.type}`, location)})`)\n" +
        "        .on(\"mouseover\", function(d, i) {\n" +
        "            div.transition()\n" +
        "                .duration(200)\n" +
        "                .style(\"opacity\", .9);\n" +
        "            div\t.html(i.type)\n" +
        "                .style(\"left\", (event.pageX) + \"px\")\n" +
        "                .style(\"top\", (event.pageY - 28) + \"px\");\n" +
        "        })\n" +
        "        .on(\"mouseout\", function(d) {\n" +
        "            div.transition()\n" +
        "                .duration(500)\n" +
        "                .style(\"opacity\", 0);\n" +
        "        });\n" +
        "\n" +
        "    const node = svg.append(\"g\")\n" +
        "        .attr(\"fill\", \"currentColor\")\n" +
        "        .attr(\"stroke-linecap\", \"round\")\n" +
        "        .attr(\"stroke-linejoin\", \"round\")\n" +
        "        .selectAll(\"g\")\n" +
        "        .data(nodes)\n" +
        "        .join(\"g\")\n" +
        "        .call(drag(simulation));\n" +
        "\n" +
        "    node.append(\"circle\")\n" +
        "        .attr(\"stroke\", \"white\")\n" +
        "        .attr(\"stroke-width\", 1.5)\n" +
        "        .attr(\"r\", 4);\n" +
        "\n" +
        "    node.append(\"text\")\n" +
        "        .attr(\"x\", 8)\n" +
        "        .attr(\"y\", \"0.31em\")\n" +
        "        .text(d => d.id)\n" +
        "        .clone(true).lower()\n" +
        "        .attr(\"fill\", \"none\")\n" +
        "        .attr(\"stroke\", \"white\")\n" +
        "        .attr(\"stroke-width\", 3);\n" +
        "\n" +
        "    simulation.on(\"tick\", () => {\n" +
        "        link.attr(\"d\", linkArc);\n" +
        "        node.attr(\"transform\", d => `translate(${d.x},${d.y})`);\n" +
        "    });\n" +
        "</script>\n" +
        "<body></body>\n";
      sb.append(post);
      fw.write(sb.toString());
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }
}
