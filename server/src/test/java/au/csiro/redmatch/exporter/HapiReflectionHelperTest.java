/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.exporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGenderEnumFactory;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Specimen.SpecimenCollectionComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TriggerDefinition;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemFilterComponent;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.junit.BeforeClass;
import org.junit.Test;

import au.csiro.redmatch.model.grammar.redmatch.Attribute;
import ca.uhn.fhir.context.FhirContext;

/**
 * Unit tests for {@link HapiReflectionHelper}.
 * 
 * 
 * @author Alejandro Metke
 *
 */
public class HapiReflectionHelperTest {
  
  private static HapiReflectionHelper helper;
  
  @BeforeClass
  public static void init() {
    helper = new HapiReflectionHelper();
    FhirContext ctx = FhirContext.forR4();
    helper.setCtx(ctx);
    helper.init();
  }
  
  @Test
  public void testIsPrimitive() {
    // A resource
    assertFalse(helper.isPrimitive(Patient.class));
    
    // Complex types
    assertFalse(helper.isPrimitive(Quantity.class));
    assertFalse(helper.isPrimitive(Attachment.class));
    
    // A backbone element
    assertFalse(helper.isPrimitive(ObservationComponentComponent.class));
    
    // A reference
    assertFalse(helper.isPrimitive(Reference.class));
    
    // A simple type
    assertTrue(helper.isPrimitive(CodeType.class));
    
    // An enumeration
    assertTrue(helper.isPrimitive(Enumeration.class));
  }
  
  @Test
  public void testGetParametrisedType() {
    try {
      Field patientNameField = Patient.class.getDeclaredField("name");
      Class<?> res = helper.getParametrisedType(patientNameField);
      assertEquals(HumanName.class, res);
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
  }
  
  @Test
  public void testGetDeclaredType() {
    try {
      // Attribute in the class
      assertEquals(CodeableConcept.class, helper.getDeclaredType(Patient.class, "maritalStatus"));
      
      // Attribute in parent class
      assertEquals(List.class, helper.getDeclaredType(Patient.class, "contained"));
      
      // Attribute in ancestor
      assertEquals(UriType.class, helper.getDeclaredType(Patient.class, "implicitRules"));
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    // Nonexistent attribute
    try {
      assertEquals(UriType.class, helper.getDeclaredType(Patient.class, "xxx"));
      assertTrue("Expected exception but didn't get one.", false);
    } catch (NoSuchFieldException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetHapiTypes() {
    try {
      // backbone element - should return no types
      Class<? extends Base>[] types =  helper.getHapiTypes(CodeSystem.class, "filter");
      assertEquals(0, types.length);
      
      // normal value[x] - should multiple types
      types =  helper.getHapiTypes(Patient.class, "deceased");
      assertEquals(2, types.length);
      assertEquals(BooleanType.class, types[0]);
      assertEquals(DateTimeType.class, types[1]);
      
      // unconstrained value[x] - should return no types
      types =  helper.getHapiTypes(Extension.class, "value");
      assertEquals(0, types.length);
      
      // normal element
      types =  helper.getHapiTypes(CodeSystemFilterComponent.class, "operator");
      assertEquals(1, types.length);
      assertEquals(CodeType.class, types[0]);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }

    
    // Nonexistent attribute
    try {
      assertEquals(UriType.class, helper.getDeclaredType(Patient.class, "xxx"));
      assertTrue("Expected exception but didn't get one.", false);
    } catch (NoSuchFieldException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetGetMethod() {
    try {
      // Lists - plain
      Method m = helper.getGetMethod(Patient.class, "name");
      assertEquals("getName", m.getName());
      
      // Enumerations - plain
      m = helper.getGetMethod(Patient.class, "gender");
      assertEquals("getGenderElement", m.getName());
      
      // Primitive type - add Element suffix
      m = helper.getGetMethod(Patient.class, "active");
      assertEquals("getActiveElement", m.getName());
      
      // Complex type - plain
      m = helper.getGetMethod(ContactComponent.class, "name");
      assertEquals("getName", m.getName());
      
      // Backbone element - plain
      m = helper.getGetMethod(Specimen.class, "collection");
      assertEquals("getCollection", m.getName());
      
      // [x] value with primitive type - add Type suffix
      m = helper.getGetMethod(Patient.class, "deceasedBoolean");
      assertEquals("getDeceasedBooleanType", m.getName());
      
      // Reference - plain
      m = helper.getGetMethod(Specimen.class, "subject");
      assertEquals("getSubject", m.getName());
      
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.getGetMethod(Patient.class, "xxx");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetAddMethod() {
    try {
      Method m = helper.getAddMethod(Patient.class, "name");
      assertEquals("addName", m.getName());
      
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.getAddMethod(Patient.class, "gender");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
    
    try {
      helper.getAddMethod(Patient.class, "xxx");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetHasMethod() {
    try {
      Method m = helper.getHasMethod(Patient.class, "gender");
      assertEquals("hasGenderElement", m.getName());
      
      m = helper.getHasMethod(Patient.class, "deceasedBoolean");
      assertEquals("hasDeceasedBooleanType", m.getName());
    } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.getHasMethod(Patient.class, "xxx");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetSetMethod() {
    try {
      Method m = helper.getSetMethod(Patient.class, "gender", Enumeration.class, false, false);
      assertEquals("setGenderElement", m.getName());
      
      m = helper.getSetMethod(Patient.class, "gender", CodeType.class, false, false);
      assertEquals("setGenderElement", m.getName());
      
      m = helper.getSetMethod(Patient.class, "photo", Attachment.class, true, false);
      assertEquals("setPhoto", m.getName());
      
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.getSetMethod(Patient.class, "photo", Quantity.class, true, false);
      assertTrue("Expected exception but didn't get one.", false);
    } catch (NoSuchMethodException e) {
      assertTrue(true);
    }
    
    try {
      helper.getSetMethod(Patient.class, "genderr", Enumeration.class, false, false);
      assertTrue("Expected exception but didn't get one.", false);
    } catch (NoSuchMethodException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetValueXAttributeType() {
    try {
      Class<? extends Base> type = helper.getValueXAttributeType("definitionTriggerDefinition");
      assertEquals(TriggerDefinition.class, type);
      
      type = helper.getValueXAttributeType("definitionTriggerDefinitionn");
      assertNull(type);
      
      type = helper.getValueXAttributeType("valueMoney");
      assertEquals(Money.class, type);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
  }
  
  @Test
  public void testGetField() {
    try {
      Field f = helper.getField(Patient.class, "gender");
      assertEquals("gender", f.getName());
      
      f = helper.getField(CodeSystemFilterComponent.class, "operator");
      assertEquals("operator", f.getName());
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.getField(Patient.class, "genderr");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (NoSuchFieldException e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testInvokeGetter() {
    Patient p = new Patient();
    try {
      Object b = helper.invokeGetter(p, "gender");
      assertTrue(b instanceof Enumeration);
      
      b = helper.invokeGetter(p,  "contact");
      assertTrue(b instanceof List);
      
      b = helper.invokeGetter(p, "deceasedBoolean");
      assertTrue(b instanceof BooleanType);
      
      b = helper.invokeGetter(p, "maritalStatus");
      assertTrue(b instanceof CodeableConcept);
      
      b = helper.invokeGetter(p,  "managingOrganization");
      assertTrue(b instanceof Reference);
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.invokeGetter(p, "xxx");
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testInvokeSetter() {
    Patient p = new Patient();
    try {
      
      // Enumeration, single value
      Enumeration<AdministrativeGender> ag = 
          new Enumeration<>(new AdministrativeGenderEnumFactory(), "male");
      Object b = helper.invokeSetter(p, "gender", ag, false, null, false);
      assertEquals(AdministrativeGender.MALE, p.getGender());
      
      // List with no index
      ContactComponent cc = new ContactComponent();
      cc.getName().setFamily("Doe");
      b = helper.invokeSetter(p, "contact", cc, true, null, false);
      assertTrue(b instanceof Patient);
      assertTrue(p.hasContact());
      assertEquals(1, p.getContact().size());
      assertEquals("Doe", p.getContactFirstRep().getName().getFamily());
      
      // List with index - enough elements in list
      cc = new ContactComponent();
      cc.getName().setFamily("Smith");
      b = helper.invokeSetter(p, "contact", cc, true, 0, false);
      assertTrue(b instanceof Patient);
      assertEquals(1, p.getContact().size());
      assertEquals("Smith", p.getContactFirstRep().getName().getFamily());
      
      // List with index - not enough elements in list
      cc = new ContactComponent();
      cc.getName().setFamily("Griffin");
      b = helper.invokeSetter(p, "contact", cc, true, 4, false);
      assertTrue(b instanceof Patient);
      assertEquals(5, p.getContact().size());
      assertEquals("Smith", p.getContact().get(0).getName().getFamily());
      assertEquals("Griffin", p.getContact().get(4).getName().getFamily());
      
      // [x] value
      BooleanType deceased = new BooleanType(false);
      b = helper.invokeSetter(p, "deceased", deceased, false, null, true);
      assertTrue(b instanceof Patient);
      assertTrue(p.hasDeceased());
      assertFalse(p.getDeceasedBooleanType().booleanValue());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    try {
      helper.invokeSetter(p, "xxx", new StringType("xxx"), false, null, false);
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
    
    try {
      // [x] value with wrong type
      StringType deceased = new StringType("false");
      helper.invokeSetter(p, "deceased", deceased, false, null, true);
      assertTrue("Expected exception but didn't get one.", false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
  
  @Test
  public void testGetGenericAttributeName() {
    String s = helper.getGenericAttributeName("valueCodeableConcept");
    assertEquals("value", s);
    
    s = helper.getGenericAttributeName("participantEffectivePeriod");
    assertEquals("participantEffective", s);
    
    s = helper.getGenericAttributeName("valueXxx");
    assertNull(s);
  }
  
  @Test
  public void testIsAttributeSet() {
    Patient p1 = new Patient();
    p1.setId("p1");
    p1.addName(new HumanName().addGiven("John"));
    p1.setGender(AdministrativeGender.FEMALE);
    p1.setActive(true);
    p1.addContact().setName(new HumanName().addGiven("John Sr"));
    p1.setDeceased(new BooleanType(false));
    
    Specimen s1 = new Specimen();
    s1.setCollection(new SpecimenCollectionComponent().setCollected(new DateTimeType(new Date())));
    s1.setSubject(new Reference(p1));
    
    Patient p2 = new Patient();
    Specimen s2 = new Specimen();
    
    try {
      assertTrue(helper.isAttributeSet(p1, "name"));
      assertTrue(helper.isAttributeSet(p1, "gender"));
      assertTrue(helper.isAttributeSet(p1, "active"));
      assertTrue(helper.isAttributeSet(p1.getContactFirstRep(), "name"));
      assertTrue(helper.isAttributeSet(p1, "deceased"));
      
      assertTrue(helper.isAttributeSet(s1, "collection"));
      assertTrue(helper.isAttributeSet(s1, "subject"));
      
      
      assertFalse(helper.isAttributeSet(p2, "name"));
      assertFalse(helper.isAttributeSet(p2, "gender"));
      assertFalse(helper.isAttributeSet(p2, "active"));
      assertFalse(helper.isAttributeSet(p2.getContactFirstRep(), "name"));
      assertFalse(helper.isAttributeSet(p2, "deceased"));
      
      assertFalse(helper.isAttributeSet(s2, "collection"));
      assertFalse(helper.isAttributeSet(s2, "subject"));
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
  }
  
  @Test
  public void testGetElementToSet() {
    // Patient.contact.name - no index
    Patient p = new Patient();
    List<Attribute> atts = new ArrayList<>();
    Attribute a1 = new Attribute();
    a1.setName("contact");
    a1.setList(true);
    atts.add(a1);
    Attribute a2 = new Attribute();
    a2.setName("name");
    a2.setList(false);
    atts.add(a2);
    
    try {
      Object o = helper.getElementToSet(p, atts);
      assertTrue(o instanceof HumanName);
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    // Patient.contact.name - with index, empty list
    p = new Patient();
    atts = new ArrayList<>();
    a1 = new Attribute();
    a1.setName("contact");
    a1.setList(true);
    a1.setAttributeIndex(4);
    atts.add(a1);
    a2 = new Attribute();
    a2.setName("name");
    a2.setList(false);
    atts.add(a2);
    
    try {
      Object o = helper.getElementToSet(p, atts);
      assertTrue(o instanceof HumanName);
      ((HumanName) o).setFamily("Griffin");
      assertEquals(5, p.getContact().size());
      assertTrue(p.getContact().get(4).hasName());
      for (int i = 0; i < 4; i++) {
        assertFalse(p.getContact().get(i).hasName());
      }
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
    
    // Patient.contact.name - with index, populated list
    p = new Patient();
    p.addContact().setName(new HumanName().setFamily("Smith"));
    p.addContact().setName(new HumanName().setFamily("Doe"));
    atts = new ArrayList<>();
    a1 = new Attribute();
    a1.setName("contact");
    a1.setList(true);
    a1.setAttributeIndex(1);
    atts.add(a1);
    a2 = new Attribute();
    a2.setName("name");
    a2.setList(false);
    atts.add(a2);
    
    try {
      Object o = helper.getElementToSet(p, atts);
      assertTrue(o instanceof HumanName);
      assertEquals(2, p.getContact().size());
      assertEquals("Doe", p.getContact().get(1).getName().getFamily());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: " + e.getLocalizedMessage(), false);
    }
  }

}
