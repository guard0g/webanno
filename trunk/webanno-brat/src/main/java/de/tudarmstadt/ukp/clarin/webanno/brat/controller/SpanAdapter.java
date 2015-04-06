/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.clarin.webanno.brat.controller;

import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.getAddr;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.getFeature;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.getFirstSentenceNumber;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.getLastSentenceAddressInDisplayWindow;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.isSameSentence;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.selectByAddr;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.selectOverlapping;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.selectSentenceAt;
import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.setFeature;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureStructureImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.BratAnnotatorModel;
import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.component.AnnotationDetailEditorPanel.LinkWithRoleModel;
import de.tudarmstadt.ukp.clarin.webanno.brat.display.model.Argument;
import de.tudarmstadt.ukp.clarin.webanno.brat.display.model.Entity;
import de.tudarmstadt.ukp.clarin.webanno.brat.display.model.Offsets;
import de.tudarmstadt.ukp.clarin.webanno.brat.display.model.Relation;
import de.tudarmstadt.ukp.clarin.webanno.brat.message.GetDocumentResponse;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationLayer;
import de.tudarmstadt.ukp.clarin.webanno.model.LinkMode;
import de.tudarmstadt.ukp.clarin.webanno.model.Mode;
import de.tudarmstadt.ukp.clarin.webanno.model.MultiValueMode;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * A class that is used to create Brat Span to CAS and vice-versa
 *
 * @author Seid Muhie Yimam
 * @author Richard Eckart de Castilho
 */
public class SpanAdapter
    implements TypeAdapter
{
    /**
     * The minimum offset of the annotation is on token, and the annotation can't span multiple
     * tokens too
     */
    private boolean lockToTokenOffsets;

    /**
     * The minimum offset of the annotation is on token, and the annotation can span multiple token
     * too
     */
    private boolean allowMultipleToken;

    /**
     * Allow multiple annotations of the same layer (only when the type value is different)
     */
    private boolean allowStacking;

    private boolean crossMultipleSentence;

    private boolean deletable;

    private AnnotationLayer layer;
    
    private Map<String, AnnotationFeature> features;

    // value NILL for a token when the training file do not have annotations provided
    private final static String NILL = "__nill__";

    public SpanAdapter(AnnotationLayer aLayer, Collection<AnnotationFeature> aFeatures)
    {
        layer = aLayer;
        
        features = new LinkedHashMap<String, AnnotationFeature>();
        for (AnnotationFeature f : aFeatures) {
            features.put(f.getName(), f);
        }
    }

    /**
     * Span can only be made on a single token (not multiple tokens), e.g. for POS or Lemma
     * annotations. If this is set and a span is made across multiple tokens, then one annotation of
     * the specified type will be created for each token. If this is not set, a single annotation
     * covering all tokens is created.
     *
     * @param aSingleTokenBehavior whether to enable the behavior.
     */
    public void setLockToTokenOffsets(boolean aSingleTokenBehavior)
    {
        lockToTokenOffsets = aSingleTokenBehavior;
    }

    /**
     * @return whether the behavior is enabled.
     * @see #setLockToTokenOffsets(boolean)
     */
    public boolean isLockToTokenOffsets()
    {
        return lockToTokenOffsets;
    }

    public boolean isAllowMultipleToken()
    {
        return allowMultipleToken;
    }

    public void setAllowMultipleToken(boolean allowMultipleToken)
    {
        this.allowMultipleToken = allowMultipleToken;
    }

    public boolean isAllowStacking()
    {
        return allowStacking;
    }

    public void setAllowStacking(boolean allowStacking)
    {
        this.allowStacking = allowStacking;
    }

    public boolean isCrossMultipleSentence()
    {
        return crossMultipleSentence;
    }

    public void setCrossMultipleSentence(boolean crossMultipleSentence)
    {
        this.crossMultipleSentence = crossMultipleSentence;
    }

    /**
     * Add annotations from the CAS, which is controlled by the window size, to the brat response
     * {@link GetDocumentResponse}
     *
     * @param aJcas
     *            The JCAS object containing annotations
     * @param aResponse
     *            A brat response containing annotations in brat protocol
     * @param aBratAnnotatorModel
     *            Data model for brat annotations
     * @param aColoringStrategy
     *            the coloring strategy to render this layer
     */
    @Override
    public void render(JCas aJcas, List<AnnotationFeature> aFeatures,
            GetDocumentResponse aResponse, BratAnnotatorModel aBratAnnotatorModel,
            ColoringStrategy aColoringStrategy)
    {
        // The first sentence address in the display window!
        Sentence firstSentence = selectSentenceAt(aJcas,
                aBratAnnotatorModel.getSentenceBeginOffset(),
                aBratAnnotatorModel.getSentenceEndOffset());

        int lastAddressInPage = getLastSentenceAddressInDisplayWindow(aJcas,
                firstSentence.getAddress(), aBratAnnotatorModel.getWindowSize());

        // the last sentence address in the display window
        Sentence lastSentenceInPage;
        if (aBratAnnotatorModel.getMode().equals(Mode.CURATION)) {
            lastSentenceInPage = firstSentence;
        }
        else {
            lastSentenceInPage = (Sentence) selectByAddr(aJcas, FeatureStructure.class,
                    lastAddressInPage);
        }

        Type type = getType(aJcas.getCas(), getAnnotationTypeName());
        int aFirstSentenceOffset = firstSentence.getBegin();

        for (AnnotationFS fs : selectCovered(aJcas.getCas(), type, firstSentence.getBegin(),
                lastSentenceInPage.getEnd())) {
            String bratTypeName = TypeUtil.getBratTypeName(this);
            String bratLabelText = TypeUtil.getBratLabelText(this, fs, aFeatures);
            String color = aColoringStrategy.getColor(fs, bratLabelText);

            Sentence beginSent = null, endSent = null;
            // check if annotation spans multiple sentence
            for (Sentence sentence : selectCovered(aJcas, Sentence.class, firstSentence.getBegin(),
                    lastSentenceInPage.getEnd())) {
                if (sentence.getBegin() <= fs.getBegin() && fs.getBegin() <= sentence.getEnd()) {
                    beginSent = sentence;
                    break;
                }
            }
            for (Sentence sentence : selectCovered(aJcas, Sentence.class, firstSentence.getBegin(),
                    lastSentenceInPage.getEnd())) {
                if (sentence.getBegin() <= fs.getEnd() && fs.getEnd() <= sentence.getEnd()) {
                    endSent = sentence;
                    break;
                }
            }
            
            List<Sentence> sentences = selectCovered(aJcas, Sentence.class, beginSent.getBegin(),
                    endSent.getEnd());
            List<Offsets> offsets = new ArrayList<Offsets>();
            if (sentences.size() > 1) {
                for (Sentence sentence : sentences) {
                    if (sentence.getBegin() <= fs.getBegin() && fs.getBegin() <= sentence.getEnd()) {
                        offsets.add(new Offsets(fs.getBegin() - aFirstSentenceOffset, sentence
                                .getEnd() - aFirstSentenceOffset));
                    }
                    else if (sentence.getBegin() <= fs.getEnd() && fs.getEnd() <= sentence.getEnd()) {
                        offsets.add(new Offsets(sentence.getBegin() - aFirstSentenceOffset, fs
                                .getEnd() - aFirstSentenceOffset));
                    }
                    else {
                        offsets.add(new Offsets(sentence.getBegin() - aFirstSentenceOffset,
                                sentence.getEnd() - aFirstSentenceOffset));
                    }
                }
            aResponse
                    .addEntity(new Entity(getAddr(fs), bratTypeName, offsets, bratLabelText, color));
                        }
            else {
                // FIXME It should be possible to remove this case and the if clause because
                // the case that a FS is inside a single sentence is just a special case
                aResponse.addEntity(new Entity(getAddr(fs), bratTypeName, new Offsets(fs.getBegin()
                        - aFirstSentenceOffset, fs.getEnd() - aFirstSentenceOffset), bratLabelText,
                        color));
            }
                
            // Render slots
            int fi = 0;
            for (AnnotationFeature feat : listFeatures()) {
                if (MultiValueMode.ARRAY.equals(feat.getMultiValueMode())
                        && LinkMode.WITH_ROLE.equals(feat.getLinkMode())) {
                    List<LinkWithRoleModel> links = getFeature(fs, feat);
                    for (int li = 0; li < links.size(); li++) {
                        LinkWithRoleModel link = links.get(li);
                        FeatureStructure targetFS = selectByAddr(fs.getCAS(), link.targetAddr);
                        
                        // HACK: here we generate a fake address... we MUST find a better mechanism
                        // before releasing this.
                        int fakeAddr = -(getAddr(fs) * 100000 + fi * 1000 + li);
                        
                        aResponse.addRelation(new Relation(fakeAddr, bratTypeName, getArgument(
                                fs, targetFS), link.role, color));
                    }
                }
                fi++;
            }
        }
    }

    /**
     * Argument lists for the arc annotation
     *
     * @return
     */
    private List<Argument> getArgument(FeatureStructure aGovernorFs, FeatureStructure aDependentFs)
    {
        return asList(new Argument("Arg1", ((FeatureStructureImpl) aGovernorFs).getAddress()),
                new Argument("Arg2", ((FeatureStructureImpl) aDependentFs).getAddress()));
    }

    public static void renderTokenAndSentence(JCas aJcas, GetDocumentResponse aResponse,
            BratAnnotatorModel aBratAnnotatorModel)
    {
        // The first sentence address in the display window!
        Sentence firstSentence = selectSentenceAt(aJcas,
                aBratAnnotatorModel.getSentenceBeginOffset(),
                aBratAnnotatorModel.getSentenceEndOffset());

        int lastAddressInPage = getLastSentenceAddressInDisplayWindow(aJcas,
                firstSentence.getAddress(), aBratAnnotatorModel.getWindowSize());

        // the last sentence address in the display window
        Sentence lastSentenceInPage;
        if (aBratAnnotatorModel.getMode().equals(Mode.CURATION)) {
            lastSentenceInPage = firstSentence;
        }
        else {
            lastSentenceInPage = (Sentence) selectByAddr(aJcas,
                    FeatureStructure.class, lastAddressInPage);
        }

        int sentenceNumber = getFirstSentenceNumber(aJcas,
                firstSentence.getAddress());
        aResponse.setSentenceNumberOffset(sentenceNumber);

        int aFirstSentenceOffset = firstSentence.getBegin();

        // Render token + texts
        for (AnnotationFS fs : selectCovered(aJcas, Token.class, firstSentence.getBegin(),
                lastSentenceInPage.getEnd())) {
            aResponse.addToken(fs.getBegin() - aFirstSentenceOffset, fs.getEnd()
                    - aFirstSentenceOffset);
        }
        aResponse.setText(aJcas.getDocumentText().substring(aFirstSentenceOffset,
                lastSentenceInPage.getEnd()));

        // Render Sentence
        for (AnnotationFS fs : selectCovered(aJcas, Sentence.class, firstSentence.getBegin(),
                lastSentenceInPage.getEnd())) {
            aResponse.addSentence(fs.getBegin() - aFirstSentenceOffset, fs.getEnd()
                    - aFirstSentenceOffset);
        }
    }

    /**
     * Add new span annotation into the CAS and return the the id of the span annotation
     *
     * @param aJcas
     *            the JCas.
     * @param aBegin
     *            the begin offset.
     * @param aEnd
     *            the end offset.
     * @param aFeature
     *            the feature.
     * @param aValue
     *            the value of the annotation for the span
     * @return the ID.
     * @throws BratAnnotationException
     *             if the annotation cannot be created/updated.
     */
    public Integer add(JCas aJcas, int aBegin, int aEnd, AnnotationFeature aFeature, Object aValue)
        throws BratAnnotationException
    {
        if (crossMultipleSentence || isSameSentence(aJcas, aBegin, aEnd)) {
            if (lockToTokenOffsets) {
                List<Token> tokens = selectOverlapping(aJcas, Token.class, aBegin,
                        aEnd);

                if (tokens.isEmpty()) {
                    throw new BratAnnotationException("No token is found to annotate");
                }
                return updateCas(aJcas.getCas(), tokens.get(0).getBegin(), tokens.get(0).getEnd(),
                        aFeature, aValue);

            }
            else if (allowMultipleToken) {
                List<Token> tokens = selectOverlapping(aJcas, Token.class, aBegin,
                        aEnd);
                // update the begin and ends (no sub token selection
                aBegin = tokens.get(0).getBegin();
                aEnd = tokens.get(tokens.size() - 1).getEnd();
                return updateCas(aJcas.getCas(), aBegin, aEnd, aFeature, aValue);
            }
            else {
                return updateCas(aJcas.getCas(), aBegin, aEnd, aFeature, aValue);
            }
        }
        else {
            throw new MultipleSentenceCoveredException("Annotation coveres multiple sentences, "
                    + "limit your annotation to single sentence!");
        }
    }

    /**
     * A Helper method to add annotation to CAS
     */
    private Integer updateCas(CAS aCas, int aBegin, int aEnd, AnnotationFeature aFeature,
            Object aValue)
    {
        Type type = CasUtil.getType(aCas, getAnnotationTypeName());
        for (AnnotationFS fs : CasUtil.selectCovered(aCas, type, aBegin, aEnd)) {
            if (fs.getBegin() == aBegin && fs.getEnd() == aEnd) {
                if (!allowStacking) {
                    setFeature(fs, aFeature, aValue);
                    return ((FeatureStructureImpl) fs).getAddress();
                }
            }
        }
        AnnotationFS newAnnotation = aCas.createAnnotation(type, aBegin, aEnd);
        setFeature(newAnnotation, aFeature, aValue);

        if (getAttachFeatureName() != null) {
            Type theType = CasUtil.getType(aCas, getAttachTypeName());
            Feature attachFeature = theType.getFeatureByBaseName(getAttachFeatureName());
            // if the attache type feature structure is not in place
            // (for custom annotation), create it
            if (CasUtil.selectCovered(aCas, theType, aBegin, aEnd).size() == 0) {
                AnnotationFS attachTypeAnnotation = aCas.createAnnotation(theType, aBegin, aEnd);
                aCas.addFsToIndexes(attachTypeAnnotation);

            }
            CasUtil.selectCovered(aCas, theType, aBegin, aEnd).get(0)
                    .setFeatureValue(attachFeature, newAnnotation);
        }
        aCas.addFsToIndexes(newAnnotation);
        return ((FeatureStructureImpl) newAnnotation).getAddress();
    }

    @Override
    public void delete(JCas aJCas, int aAddress)
    {
        FeatureStructure fs = selectByAddr(aJCas, FeatureStructure.class, aAddress);
        aJCas.removeFsFromIndexes(fs);

        // delete associated attachFeature
        if (getAttachTypeName() == null) {
            return;
        }
        Type theType = CasUtil.getType(aJCas.getCas(), getAttachTypeName());
        Feature attachFeature = theType.getFeatureByBaseName(getAttachFeatureName());
        if (attachFeature == null) {
            return;
        }
        CasUtil.selectCovered(aJCas.getCas(), theType, ((AnnotationFS) fs).getBegin(),
                ((AnnotationFS) fs).getEnd()).get(0).setFeatureValue(attachFeature, null);

    }

    @Override
    public void delete(JCas aJCas, AnnotationFeature aFeature, int aBegin, int aEnd, Object aValue)
    {
        Type type = CasUtil.getType(aJCas.getCas(), getAnnotationTypeName());
        for (AnnotationFS fs : CasUtil.selectCovered(aJCas.getCas(), type, aBegin, aEnd)) {

            if (fs.getBegin() == aBegin && fs.getEnd() == aEnd) {
                if (ObjectUtils.equals(getFeature(fs, aFeature), aValue)) {
                    delete(aJCas, ((FeatureStructureImpl) fs).getAddress());
                }
            }
        }
    }

    @Override
    public long getTypeId()
    {
        return layer.getId();
    }

    @Override
    public Type getAnnotationType(CAS cas)
    {
        return CasUtil.getType(cas, getAnnotationTypeName());
    }

    /**
     * The UIMA type name.
     */
    @Override
    public String getAnnotationTypeName()
    {
        return layer.getName();
    }

    public void setDeletable(boolean aDeletable)
    {
        this.deletable = aDeletable;
    }

    @Override
    public boolean isDeletable()
    {
        return deletable;
    }

    @Override
    public String getAttachFeatureName()
    {
        return layer.getAttachFeature() == null ? null : layer.getAttachFeature().getName();
    }

    @Override
    public void deleteBySpan(JCas aJCas, AnnotationFS fs, int aBegin, int aEnd)
    {

    }

    @Override
    public List<String> getAnnotation(JCas aJcas, AnnotationFeature aFeature, int begin, int end)
    {
        Type type = getType(aJcas.getCas(), getAnnotationTypeName());
        List<String> annotations = new ArrayList<String>();

        for (Token token : selectCovered(aJcas, Token.class, begin, end)) {
            if (selectCovered(aJcas.getCas(), type, token.getBegin(), token.getEnd()).size() > 0) {
                AnnotationFS anno = selectCovered(aJcas.getCas(), type, token.getBegin(),
                        token.getEnd()).get(0);
                Feature labelFeature = anno.getType().getFeatureByBaseName(aFeature.getName());
                annotations.add(anno.getFeatureValueAsString(labelFeature));
            }
            else {
                annotations.add(NILL);
            }
        }
        return annotations;
    }

    public Map<Integer, String> getMultipleAnnotation(Sentence sentence, AnnotationFeature aFeature)
        throws CASException
    {
        Map<Integer, String> multAnno = new HashMap<Integer, String>();
        Type type = getType(sentence.getCAS(), getAnnotationTypeName());
        for (AnnotationFS fs : selectCovered(sentence.getCAS(), type, sentence.getBegin(),
                sentence.getEnd())) {
            boolean isBegin = true;
            Feature labelFeature = fs.getType().getFeatureByBaseName(aFeature.getName());
            for (Token token : selectCovered(sentence.getCAS().getJCas(), Token.class,
                    fs.getBegin(), fs.getEnd())) {
                if (multAnno.get(token.getAddress()) == null) {
                    if (isBegin) {
                        multAnno.put(token.getAddress(),
                                "B-" + fs.getFeatureValueAsString(labelFeature));
                        isBegin = false;
                    }
                    else {
                        multAnno.put(token.getAddress(),
                                "I-" + fs.getFeatureValueAsString(labelFeature));
                    }
                }
            }
        }
        return multAnno;
    }

    /**
     * A field that takes the name of the annotation to attach to, e.g.
     * "de.tudarmstadt...type.Token" (Token.class.getName())
     */
    @Override
    public String getAttachTypeName()
    {
        return layer.getAttachType() == null ? null : layer.getAttachType().getName();
    }

    @Override
    public void updateFeature(JCas aJcas, AnnotationFeature aFeature, int aAddress, Object aValue)
    {
        FeatureStructure fs = selectByAddr(aJcas, FeatureStructure.class, aAddress);
        setFeature(fs, aFeature, aValue);
    }

    @Override
    public AnnotationLayer getLayer()
    {
        return layer;
    }
    
    @Override
    public Collection<AnnotationFeature> listFeatures()
    {
        return features.values();
    }
}
