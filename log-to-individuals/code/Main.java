import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;   
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;

import java.util.ArrayList;

public class Main {
    public static final int ID_POSITION = 0;
    public static final int STANCE_POSITION = 1;
    public static final int RECEIVED_IP_LIST_POSITION = 2;
    public static final int SENT_IP_LIST_POSITION = 3;

    public static final String INPUT_LOG_FILE_NAME = "input/netlogo_output_log.csv";
    public static final String INPUT_OWL_FILE_NAME = "input/ICS.owl";
    public static final String OUTPUT_FILE_NAME = "output/ICS_with_individuals.owl";

    public static final String IP_LIST_DELIM = " ";

    public static final String ICS_NAMESPACE = "http://www.semanticweb.org/pmtucker/ontologies/2022/4/ICS";



    public ArrayList<Agent> agentList;
    public OntModel ont;

    public static void main(String[] args){
        Main main = new Main();
        main.run();
    }

    public void run() {
        this.agentList = new ArrayList<Agent>();

        this.ont = (OntModel)loadOntologyToModel(INPUT_OWL_FILE_NAME);
        
        loadCSVAgents(INPUT_LOG_FILE_NAME, "topic at tick 1");
        createModelInstances();
        saveOntology();
    }

    public Model loadOntologyToModel(String fileName){
        OntModel ont = ModelFactory.createOntologyModel();

        InputStream ontologyInputStream = RDFDataMgr.open(fileName);
        if (ontologyInputStream == null) throw new IllegalArgumentException("Could not find file \"" + fileName + "\"");

        return ont.read(ontologyInputStream, null);
    }

    public void loadCSVAgents(String fileName, String startingSequence){
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));

            while (!(br.readLine().contains(startingSequence))) continue;

            String agentRow;

            while ((agentRow = br.readLine()).startsWith("ID")) { //while we are still in current tick, as indicated by presence of IDs
                String[] agentArray = agentRow.split(",");
                
                //trim and retrieve ID
                int ID = Integer.parseInt(agentArray[ID_POSITION].replaceFirst("ID-", ""));

                //retrieve stance
                int stance = Integer.parseInt(agentArray[STANCE_POSITION]);
                
                //retrieve received IP list
                ArrayList<Integer> receivedIPList = new ArrayList<Integer>();

                String untrimmedReceivedIPList = agentArray[RECEIVED_IP_LIST_POSITION].replaceAll("[a-zA-Z]|-|\\[|\\]", "");
                if (untrimmedReceivedIPList.isEmpty()) {
                    //do nothing; list is null
                } else if (untrimmedReceivedIPList.contains(IP_LIST_DELIM)) {
                    String[] IPArray = untrimmedReceivedIPList.split(IP_LIST_DELIM);
                    for (String IP : IPArray) {
                        receivedIPList.add(Integer.parseInt(IP));
                    }
                } else receivedIPList.add(Integer.parseInt(untrimmedReceivedIPList));

                //retrieve sent IP list
                ArrayList<Integer> sentIPList = new ArrayList<Integer>();

                String untrimmedSentIPList = agentArray[SENT_IP_LIST_POSITION].replaceAll("[a-zA-Z]|-|\\[|\\]", "");
                if (untrimmedSentIPList.isEmpty()) {
                    //do nothing; list is null
                } else if (untrimmedSentIPList.contains(IP_LIST_DELIM)) {
                    String[] IPArray = untrimmedSentIPList.split(IP_LIST_DELIM);
                    for (String IP : IPArray) {
                        sentIPList.add(Integer.parseInt(IP));
                    }
                } else sentIPList.add(Integer.parseInt(untrimmedSentIPList));

                //instantiate agents and add to agent list
                Agent newAgent = new Agent(ID, stance, receivedIPList, sentIPList);
                agentList.add(newAgent);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void createModelInstances(){
        for (Agent agent : agentList) {
            //set URI and retrieve appropriate Agent class from ontology
            String agentIndividualURI = ICS_NAMESPACE + "/" + agent.getID();
            OntClass agentClass = ont.getOntClass(ICS_NAMESPACE + "#Agent");
            OntClass basicAgentClass = ont.getOntClass(ICS_NAMESPACE + "#BasicAgent");
            OntClass infoDissAgentClass = ont.getOntClass(ICS_NAMESPACE + "#InformationDisseminationAgent");
            OntClass IPClass = ont.getOntClass(ICS_NAMESPACE + "#InformationPacket");

            ObjectProperty sentIPProperty = ont.getObjectProperty(ICS_NAMESPACE + "#sendsInformationPacket");
            ObjectProperty receivedIPProperty = ont.getObjectProperty(ICS_NAMESPACE + "#receivesInformationPacket");

            //right side of assignment operator creates and adds individual to ontology; left side captures it as an object
            Individual agentIndividual;

            if (String.valueOf(agent.getID()).length() == 1) agentIndividual = ont.createIndividual(agentIndividualURI, basicAgentClass);
            else if (String.valueOf(agent.getID()).length() == 3) agentIndividual = ont.createIndividual(agentIndividualURI,infoDissAgentClass);
            else agentIndividual = ont.createIndividual(agentIndividualURI, agentClass);

            //retrieve stance property
            Property stance = ont.getProperty(ICS_NAMESPACE, "#stance");
            
            //add stance of agent to ontology
            agentIndividual.addProperty(stance, String.valueOf(agent.getStance()));

            //retrieve sent IP's, create individuals of InformationPacket for each one
            for (Integer IP : agent.getSentIPList()) {
                String IPURI = ICS_NAMESPACE + "/IP-" + String.valueOf(IP);

                Individual IPIndividual = ont.createIndividual(IPURI, IPClass);
                agentIndividual.addProperty(sentIPProperty, IPIndividual);
            }

            //retrieve received IP's, create individuals of InformationPacket for each one
            for (Integer IP : agent.getReceivedIPList()) {
                String IPURI = ICS_NAMESPACE + "/IP-" + String.valueOf(IP);

                Individual IPIndividual = ont.createIndividual(IPURI, IPClass);
                agentIndividual.addProperty(receivedIPProperty, IPIndividual);
            }

        }
    }

    public void saveOntology(){
        FileOutputStream ontologyFileOutputStream;

        try {
            ontologyFileOutputStream = new FileOutputStream(OUTPUT_FILE_NAME);
            ont.write(ontologyFileOutputStream);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
