package io.github.sanctuuary.proteomics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import nl.uu.cs.ape.sat.APE;
import nl.uu.cs.ape.sat.configuration.APEConfigException;
import nl.uu.cs.ape.sat.configuration.APERunConfig;
import nl.uu.cs.ape.sat.core.implSAT.SATsolutionsList;
import nl.uu.cs.ape.sat.utils.APEUtils;

/**
 * Hello world!
 *
 */
public class App 
{

	public static void main(String[] args) throws IOException {
		
		ConfiguringDomain.setupDomain();
		System.out.println("end");
	}
}
