/*******************************************************************************
 * Copyright 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.clarin.webanno.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Writes a specific Conll File (9 TAB separated) annotation from the CAS object. Example of output
 * file: 1 Heutzutage heutzutage ADV _ _ 2 ADV _ _ First column: token Number, in a sentence second
 * Column: the token third column: the lemma forth column: the POS fifth/sixth xolumn: Not Yet known
 * seventh column: the target token for a dependency parsing eighth column: the function of the
 * dependency parsing ninth and tenth column: Not Yet Known
 *
 * Sentences are separated by a blank new line
 *
 * @author Seid Muhie Yimam
 *
 */

public class ConllWriter
    extends JCasFileWriter_ImplBase
{

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        OutputStream docOS = null;
        try {
            docOS = getOutputStream(aJCas, filenameSuffix);
            IOUtils.write(convertToConnl(aJCas), docOS, encoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }

    }

    private String convertToConnl(JCas aJCas)
    {
        StringBuilder conllSb = new StringBuilder();
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            // Map of token and the dependent (token address used as a Key)
            Map<Integer, Integer> dependentMap = new HashMap<Integer, Integer>();
            // Map of governor token address and its token position
            Map<Integer, Integer> dependencyMap = new HashMap<Integer, Integer>();
            // Map of goverenor token address and its dependency function value
            Map<Integer, String> dependencyTypeMap = new HashMap<Integer, String>();

            for (Dependency dependecny : selectCovered(Dependency.class, sentence)) {
                dependentMap.put(dependecny.getGovernor().getAddress(), dependecny.getDependent()
                        .getAddress());
            }

            int i = 1;
            for (Dependency dependecny : selectCovered(Dependency.class, sentence)) {
                // if(dependecny.getDependencyType().equals("S")||dependecny.getDependencyType().equals("ROOT"))
                // {
                dependencyMap.put(dependecny.getGovernor().getAddress(), i);
                i++;
            }

            for (Dependency dependecny : selectCovered(Dependency.class, sentence)) {
                dependencyTypeMap.put(dependecny.getGovernor().getAddress(),
                        dependecny.getDependencyType());
            }

            int j = 1;
            // Add named Entity to a token
            Map<String, String> tokenNamedEntityMap = new HashMap<String, String>();
            for (NamedEntity namedEntity : selectCovered(NamedEntity.class, sentence)) {
                boolean sameChain = false; // maintain multiple span chains in BIO1 or BIO2
            for (Token token : selectCovered(Token.class, sentence)) {
                    if(namedEntity.getBegin()<=token.getBegin() && namedEntity.getEnd()>=token.getEnd()) {
                        if (tokenNamedEntityMap.get("first-" + token.getAddress()) == null &!sameChain ) {
                            tokenNamedEntityMap.put("first-" + token.getAddress(),
                                    namedEntity.getValue());
                        }
                        else  if (tokenNamedEntityMap.get("second-" + token.getAddress()) == null) {
                            tokenNamedEntityMap.put("second-" + token.getAddress(),
                                    namedEntity.getValue());
                            sameChain = true;
                        }
                    }
                }
            }
            String previopusNamedEntity1 = "O";
            String previopusNamedEntity2 = "O";
            for (Token token : selectCovered(Token.class, sentence)) {

                String lemma = token.getLemma() == null ? "_" : token.getLemma().getValue();
                String pos = token.getPos() == null ? "_" : token.getPos().getPosValue();
                String dependent = "_";

                String firstNamedEntity =  tokenNamedEntityMap.get("first-" + token.getAddress());
                if (firstNamedEntity==null){
                    firstNamedEntity="O";
                    previopusNamedEntity1 = "O";
                }
                else if (previopusNamedEntity1.equals("O")){
                    firstNamedEntity = "B_"+firstNamedEntity;
                    previopusNamedEntity1 = firstNamedEntity;
                }
                else if(previopusNamedEntity1.substring(2).equals(firstNamedEntity)){
                    firstNamedEntity = "I_"+firstNamedEntity;
                    previopusNamedEntity1 = firstNamedEntity;
                }
                else{
                    firstNamedEntity = "B_"+firstNamedEntity;
                    previopusNamedEntity1 = firstNamedEntity;
                }
                // for Nested Named Entity
                String secondNamedEntity =  tokenNamedEntityMap.get("second-" + token.getAddress());
                if (secondNamedEntity==null){
                    secondNamedEntity="O";
                    previopusNamedEntity2 = "O";
                }
                else if (previopusNamedEntity2.equals("O")){
                    secondNamedEntity = "B_"+secondNamedEntity;
                    previopusNamedEntity2 = secondNamedEntity;
                }
                else if(previopusNamedEntity2.substring(2).equals(secondNamedEntity)){
                    secondNamedEntity = "I_"+secondNamedEntity;
                    previopusNamedEntity2 = secondNamedEntity;
                }
                else{
                    secondNamedEntity = "B_"+secondNamedEntity;
                    previopusNamedEntity2 = secondNamedEntity;
                }

                if (dependentMap.get(token.getAddress()) != null) {
                    if (dependencyMap.get(dependentMap.get(token.getAddress())) != null) {
                        dependent = "" + dependencyMap.get(dependentMap.get(token.getAddress()));
                    }
                }
                String type = dependencyTypeMap.get(token.getAddress()) == null ? "_"
                        : dependencyTypeMap.get(token.getAddress());

                if (dependentMap.get(token.getAddress()) != null
                        && dependencyMap.get(dependentMap.get(token.getAddress())) != null
                        && j == dependencyMap.get(dependentMap.get(token.getAddress()))) {
                    conllSb.append(j + "\t" + token.getCoveredText() + "\t" + lemma + "\t" + pos
                            + "\t" + firstNamedEntity+"\t"+secondNamedEntity+"\t" + 0 + "\t" + type + "\t_\t_\n");
                }
                else {
                    conllSb.append(j + "\t" + token.getCoveredText() + "\t" + lemma + "\t" + pos
                            + "\t" + firstNamedEntity+"\t"+secondNamedEntity+"\t" + dependent + "\t" + type + "\t_\t_\n");
                }
                j++;
            }
            conllSb.append("\n");
        }

        return conllSb.toString().trim();
    }
}
