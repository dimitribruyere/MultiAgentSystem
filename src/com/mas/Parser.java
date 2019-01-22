package com.mas;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Parser
{
    List<Frequency> frequencies;
    List<Domain> domains;
    List<Constraint> constraints;

    public void readTextFile(String number)
    {
        frequencies = new ArrayList<>();
        domains = new ArrayList<>();
        constraints = new ArrayList<>();

        try{
            String path = "C:/Users/Marine/Documents/M2/Multi Agent Systems/FullRLFAP/CELAR/scen" + number + "/dom.txt";
            InputStream flux = new FileInputStream(path);
            InputStreamReader lecture = new InputStreamReader(flux);
            BufferedReader buff = new BufferedReader(lecture);
            String line;
            buff.readLine(); //Ignore the first line which contains the union of the domains.
            while ((line=buff.readLine())!=null){
                List<Integer> values = splitLineToInt(line);
                Domain domain = new Domain();
                domain.setId(values.get(0));
                domain.setSize(values.get(1));
                List<Integer> frequencies = new ArrayList<>();
                for (int i=2; i<values.size(); i++)
                {
                    frequencies.add(values.get(i));
                }
                domain.setValues(frequencies);
                domains.add(domain);
            }

            path = "C:/Users/Marine/Documents/M2/Multi Agent Systems/FullRLFAP/CELAR/scen" + number + "/var.txt";
            flux = new FileInputStream(path);
            lecture = new InputStreamReader(flux);
            buff = new BufferedReader(lecture);
            while ((line=buff.readLine())!=null){
                List<Integer> values = splitLineToInt(line);
                Frequency frequency = new Frequency();
                frequency.setId(values.get(0));
                frequency.setDomain(values.get(1));
                frequencies.add(frequency);
            }

            path = "C:/Users/Marine/Documents/M2/Multi Agent Systems/FullRLFAP/CELAR/scen" + number + "/ctr.txt";
            flux = new FileInputStream(path);
            lecture = new InputStreamReader(flux);
            buff = new BufferedReader(lecture);
            while ((line=buff.readLine())!=null){
                List<String> values = splitLineToString(line);
                Constraint constraint = new Constraint();
                constraint.setF1(Integer.parseInt(values.get(0)));
                constraint.setF2(Integer.parseInt(values.get(1)));
                constraint.setDifference(values.get(2).equals("D"));
                constraint.setValue(Integer.parseInt(values.get(4)));
                if(values.size()==6){
                    constraint.setCost(Integer.parseInt(values.get(5)));
                } else {
                    constraint.setCost(-1);
                }
                constraints.add(constraint);
            }
            buff.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public static List<Integer> splitLineToInt(String line){
        List<String> data = Arrays.asList(line.split(" "));
        List<Integer> values = new ArrayList<>();
        for (String val : data) {
            if (!val.equals("")){
                values.add(Integer.parseInt(val));
            }
        }
        return values;
    }

    public void createXML(String number){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();

            int a1=1, a2=1, a3=1, a4=1;
            if(number.equals("04") || number.equals("05") || number.equals("06")|| number.equals("09") || number.equals("11"))
            {
                a1 = 1000;
                a2 = 100;
                a3 = 10;
                a4 = 1;
            }
            if(number.equals("07"))
            {
                a1 = 1000000;
                a2 = 10000;
                a3 = 100;
                a4 = 1;
            }
            if(number.equals("08"))
            {
                a1 = 4;
                a2 = 3;
                a3 = 2;
                a4 = 1;
            }
            if(number.equals("10"))
            {
                a1 = 1000;
                a2 = 100;
                a3 = 2;
                a4 = 1;
            }

            //Root element
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("instance");
            doc.appendChild(root);

            //Presentation
            Element presentation = doc.createElement("presentation");
            presentation.setAttribute("name", "sampleProblem");
            presentation.setAttribute("maxConstraintArity", "2");
            presentation.setAttribute("maximize", "false");
            presentation.setAttribute("format", "XCSP 2.1_FRODO");
            root.appendChild(presentation);

            //Agents
            Element agentsElement = doc.createElement("agents");
            agentsElement.setAttribute("nbAgents", String.valueOf(frequencies.size()));
            root.appendChild(agentsElement);
            for (Frequency frequency : frequencies){
                Element agent = doc.createElement("agent");
                agent.setAttribute("name", "agent"+frequency.getId());
                agentsElement.appendChild(agent);
            }

            //Domains
            Element domainsElement = doc.createElement("domains");
            domainsElement.setAttribute("nbDomains", String.valueOf(domains.size()));
            root.appendChild(domainsElement);
            for (Domain d : domains){
                Element domain = doc.createElement("domain");
                domain.setAttribute("name", "domain"+d.getId());
                domain.setAttribute("nbValues", String.valueOf(d.getSize()));
                String freq = "";
                for (Integer f : d.getValues()){
                    freq += String.valueOf(f) + " ";
                }
                domain.appendChild(doc.createTextNode(freq));
                domainsElement.appendChild(domain);
            }

            //Variables
            Element variablesElement = doc.createElement("variables");
            variablesElement.setAttribute("nbVariables", String.valueOf(frequencies.size()));
            root.appendChild(variablesElement);
            for (Frequency frequency : frequencies){
                Element variable = doc.createElement("variable");
                variable.setAttribute("name", "f"+frequency.getId());
                variable.setAttribute("domain", "domain"+frequency.getDomain());
                variable.setAttribute("agent", "agent"+frequency.getId());
                variablesElement.appendChild(variable);
            }

            //Predicates
            Element predicatesElement = doc.createElement("predicates");
            predicatesElement.setAttribute("nbPredicates", "3");
            root.appendChild(predicatesElement);

            //Predicate EQUAL => Hard constraint
            Element predicate = doc.createElement("predicate");
            predicate.setAttribute("name", "EQUAL");
            Element parameters = doc.createElement("parameters");
            parameters.appendChild(doc.createTextNode(" int F1 int F2 int VALUE "));
            predicate.appendChild(parameters);
            Element expression = doc.createElement("expression");
            Element functional = doc.createElement("functional");
            functional.appendChild(doc.createTextNode(" eq(abs(sub(F1, F2)),VALUE) "));
            expression.appendChild(functional);
            predicate.appendChild(expression);
            predicatesElement.appendChild(predicate);

            //Predicate GTHARD => Hard constraint
            predicate = doc.createElement("predicate");
            predicate.setAttribute("name", "GTHARD");
            parameters = doc.createElement("parameters");
            parameters.appendChild(doc.createTextNode(" int F1 int F2 int VALUE "));
            predicate.appendChild(parameters);
            expression = doc.createElement("expression");
            functional = doc.createElement("functional");
            functional.appendChild(doc.createTextNode(" gt(abs(sub(F1, F2)),VALUE) "));
            expression.appendChild(functional);
            predicate.appendChild(expression);
            predicatesElement.appendChild(predicate);

            //Predicate GTSOFT => soft constraint
            predicate = doc.createElement("function");
            predicate.setAttribute("name", "GTSOFT");
            parameters = doc.createElement("parameters");
            parameters.appendChild(doc.createTextNode(" int F1 int F2 int VALUE int COST"));
            predicate.appendChild(parameters);
            expression = doc.createElement("expression");
            functional = doc.createElement("functional");
            functional.appendChild(doc.createTextNode(" if(gt(abs(sub(F1, F2)),VALUE),0, COST) "));
            expression.appendChild(functional);
            predicate.appendChild(expression);
            predicatesElement.appendChild(predicate);

            //Constraints
            Element constraintsElement = doc.createElement("constraints");
            constraintsElement.setAttribute("nbConstraints", String.valueOf(constraints.size()));
            root.appendChild(constraintsElement);
            for (Constraint c : constraints){
                Element constraint = doc.createElement("constraint");
                constraint.setAttribute("name", c.getF1()+"_"+c.getF2()+"_"+c.getValue());
                constraint.setAttribute("arity", "2");
                constraint.setAttribute("scope", "f"+c.getF1()+" f"+c.getF2());
                if (c.isDifference()){
                    constraint.setAttribute("reference", "EQUAL");
                }
                else if (c.getCost()!= -1) {
                    constraint.setAttribute("reference", "GTSOFT");
                } else {
                    constraint.setAttribute("reference", "GTHARD");
                }
                Element param = doc.createElement("parameters");
                int cost=-1;
                switch (c.getCost()){
                    case 1:
                        cost = a1;
                        break;
                    case 2:
                        cost = a2;
                        break;
                    case 3:
                        cost = a3;
                        break;
                    case 4:
                        cost = a4;
                        break;
                }
                String params = "f"+c.getF1()+" f"+c.getF2()+ " "+c.getValue()+ (cost!=-1?" "+cost:"");
                param.appendChild(doc.createTextNode(params));
                constraint.appendChild(param);
                constraintsElement.appendChild(constraint);
        }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("XML/XCSPscen"+number+".xml"));
            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public static List<String> splitLineToString(String line){
        List<String> data = Arrays.asList(line.split(" "));
        return data.stream().filter(d -> !d.equals("")).collect(Collectors.toList());
    }


}
