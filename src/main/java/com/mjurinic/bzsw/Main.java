package com.mjurinic.bzsw;

import com.mjurinic.bzsw.helpers.OntologyHelper;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String ONTOLOGY_BASE_IRI = "http://mjurinic.com/bzsw/ontologies/example.owl";

    private static OntologyHelper ontologyHelper = OntologyHelper.getInstance();

    public static void main(String[] args) {
        // Create and Save new ontology
        OWLOntology ontology = ontologyHelper.createOntology(ONTOLOGY_BASE_IRI);
        ontologyHelper.saveOntology(ontology, "example.owl");

        // Define classes
        OWLClass animalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Animal");
        OWLClass wingedAnimalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#WingedAnimal");
        OWLClass mammalAnimalClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#MammalAnimal");
        OWLClass batClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Bat");
        OWLClass mosquitoClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Mosquito");
        OWLClass waspClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Wasp");
        OWLClass bearClass = ontologyHelper.createClass(ONTOLOGY_BASE_IRI + "#Bear");

        // Create relationships
        List<OWLAxiomChange> patches = new ArrayList<>();
        patches.add(ontologyHelper.createSubClass(ontology, wingedAnimalClass, animalClass));
        patches.add(ontologyHelper.createSubClass(ontology, mammalAnimalClass, animalClass));
        patches.add(ontologyHelper.createSubClass(ontology, batClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, batClass, mammalAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, mosquitoClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, waspClass, wingedAnimalClass));
        patches.add(ontologyHelper.createSubClass(ontology, bearClass, mammalAnimalClass));

        // Disjoint wasps and mosquito's (this is not a reflexive relation so we have to do it in both directions)
        patches.add(ontologyHelper.addDisjointClass(ontology, waspClass, mosquitoClass));
        patches.add(ontologyHelper.addDisjointClass(ontology, mosquitoClass, waspClass));

        // Patch and save ontology
        updateOntology(ontology, patches);

        // Create individuals
        patches.clear();
        patches.add(ontologyHelper.createIndividual(ontology, bearClass, ONTOLOGY_BASE_IRI + "#PolarBear"));
        patches.add(ontologyHelper.createIndividual(ontology, mosquitoClass, ONTOLOGY_BASE_IRI + "#TigerMosquito"));
        patches.add(ontologyHelper.createIndividual(ontology, waspClass, ONTOLOGY_BASE_IRI + "#EuropeanHornetWasp"));
        patches.add(ontologyHelper.createIndividual(ontology, batClass, ONTOLOGY_BASE_IRI + "#Megabat"));

        updateOntology(ontology, patches);

        // Create object properties
        patches.clear();
        patches.add(ontologyHelper.createObjectProperty(ontology, ONTOLOGY_BASE_IRI + "#isEating", batClass, mosquitoClass));
        patches.add(ontologyHelper.createObjectProperty(ontology, ONTOLOGY_BASE_IRI + "#isFood", mosquitoClass, batClass));

        updateOntology(ontology, patches);

        // Add object properties
        patches.clear();
        patches.add(
                 ontologyHelper.addObjectProperty(
                         ontology,
                         ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#Megabat"),
                         ontologyHelper.getObjectProperty(ONTOLOGY_BASE_IRI + "#isEating"),
                         ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"))
        );

        patches.add(
                ontologyHelper.addObjectProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"),
                        ontologyHelper.getObjectProperty(ONTOLOGY_BASE_IRI + "#isFood"),
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#Megabat"))
        );

        updateOntology(ontology, patches);

        // Create data properties
        ontologyHelper.applyChange(ontologyHelper.createDataProperty(ontology, ONTOLOGY_BASE_IRI + "#hasWings"));

        // Add data properties
        patches.clear();

        patches.add(
                ontologyHelper.addDataProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#TigerMosquito"),
                        ontologyHelper.getDataProperty(ONTOLOGY_BASE_IRI + "#hasWings"),
                        2)
        );

        patches.add(
                ontologyHelper.addDataProperty(
                        ontology,
                        ontologyHelper.getIndividual(ONTOLOGY_BASE_IRI + "#EuropeanHornetWasp"),
                        ontologyHelper.getDataProperty(ONTOLOGY_BASE_IRI + "#hasWings"),
                        4)
        );

        updateOntology(ontology, patches);
    }

    /**
     * Patch and save ontology
     * @param ontology
     * @param patches
     */
    private static void updateOntology(OWLOntology ontology, List<OWLAxiomChange> patches) {
        ontologyHelper.applyChanges(patches);
        ontologyHelper.saveOntology(ontology, "example.owl");
    }
}
