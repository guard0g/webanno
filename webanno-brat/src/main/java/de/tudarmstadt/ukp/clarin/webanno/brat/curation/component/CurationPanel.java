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
package de.tudarmstadt.ukp.clarin.webanno.brat.curation.component;

import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.selectByAddr;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationService;
import de.tudarmstadt.ukp.clarin.webanno.api.RepositoryService;
import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.BratAnnotator;
import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.BratAnnotatorModel;
import de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil;
import de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAnnotationException;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.AnnotationSelection;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationBuilder;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationContainer;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationUserSegmentForAnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationViewForSourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.brat.util.CuratorUtil;
import de.tudarmstadt.ukp.clarin.webanno.model.Mode;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Main Panel for the curation page. It displays a box with the complete text on the left side and a
 * box for a selected sentence on the right side.
 *
 * @author Andreas Straninger
 * @author Seid Muhie Yimam
 */
public class CurationPanel
    extends Panel
{
    private static final long serialVersionUID = -5128648754044819314L;

    @SpringBean(name = "jsonConverter")
    private MappingJacksonHttpMessageConverter jsonConverter;

    @SpringBean(name = "documentRepository")
    private RepositoryService repository;

    @SpringBean(name = "annotationService")
    private AnnotationService annotationService;

    public final static String CURATION_USER = "CURATION_USER";

    private CurationViewPanel sentenceOuterView;
    private BratAnnotator mergeVisualizer;

    private BratAnnotatorModel bratAnnotatorModel;

    boolean firstLoad = true;

    /**
     * Map for tracking curated spans. Key contains the address of the span, the value contains the
     * username from which the span has been selected
     */
    private Map<String, Map<Integer, AnnotationSelection>> annotationSelectionByUsernameAndAddress = new HashMap<String, Map<Integer, AnnotationSelection>>();

    private CurationViewForSourceDocument curationView;

    ListView<CurationViewForSourceDocument> textListView;

    /**
     * Class for combining an on click ajax call and a label
     */
    class AjaxLabel
        extends Label
    {

        private static final long serialVersionUID = -4528869530409522295L;
        private AbstractAjaxBehavior click;

        public AjaxLabel(String id, String label, AbstractAjaxBehavior click)
        {
            super(id, label);
            this.click = click;
        }

        @Override
        public void onComponentTag(ComponentTag tag)
        {
            // add onclick handler to the browser
            // if clicked in the browser, the function
            // click.response(AjaxRequestTarget target) is called on the server side
            tag.put("ondblclick", "Wicket.Ajax.get({'u':'" + click.getCallbackUrl() + "'})");
            tag.put("onclick", "Wicket.Ajax.get({'u':'" + click.getCallbackUrl() + "'})");
        }

    }

    public CurationPanel(String id, final CurationContainer curationContainer)
    {
        super(id);

        // add container for updating ajax
        final WebMarkupContainer textOuterView = new WebMarkupContainer("textOuterView");
        textOuterView.setOutputMarkupId(true);
        add(textOuterView);

        bratAnnotatorModel = curationContainer.getBratAnnotatorModel();

        LinkedList<CurationUserSegmentForAnnotationDocument> sentences = new LinkedList<CurationUserSegmentForAnnotationDocument>();
        CurationUserSegmentForAnnotationDocument curationUserSegmentForAnnotationDocument = new CurationUserSegmentForAnnotationDocument();
        if (bratAnnotatorModel != null) {
            curationUserSegmentForAnnotationDocument
                    .setAnnotationSelectionByUsernameAndAddress(annotationSelectionByUsernameAndAddress);
            curationUserSegmentForAnnotationDocument.setBratAnnotatorModel(bratAnnotatorModel);
            sentences.add(curationUserSegmentForAnnotationDocument);
        }
        sentenceOuterView = new CurationViewPanel("sentenceOuterView",
                new Model<LinkedList<CurationUserSegmentForAnnotationDocument>>(sentences))
        {
            private static final long serialVersionUID = 2583509126979792202L;

            @Override
            protected void onChange(AjaxRequestTarget aTarget)
            {
                try {
                    CuratorUtil.updatePanel(aTarget, this, curationContainer, mergeVisualizer,
                            repository, annotationSelectionByUsernameAndAddress, curationView,
                            annotationService, jsonConverter);
                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (ClassNotFoundException e) {
                    error(e.getMessage());
                }
                catch (IOException e) {
                    error(e.getMessage());
                }
                catch (BratAnnotationException e) {
                    error(e.getMessage());
                }
            }
        };

        sentenceOuterView.setOutputMarkupId(true);
        add(sentenceOuterView);

        mergeVisualizer = new BratAnnotator("mergeView", new Model<BratAnnotatorModel>(
                bratAnnotatorModel))
        {

            private static final long serialVersionUID = 7279648231521710155L;

            @Override
            protected void onChange(AjaxRequestTarget aTarget, BratAnnotatorModel bratAnnotatorModel)
            {
                aTarget.addChildren(getPage(), FeedbackPanel.class);
                aTarget.add(sentenceOuterView);
                try {
                    CuratorUtil.updatePanel(aTarget, sentenceOuterView, curationContainer, this,
                            repository, annotationSelectionByUsernameAndAddress, curationView,
                            annotationService, jsonConverter);
                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (ClassNotFoundException e) {
                    error(e.getMessage());
                }
                catch (IOException e) {
                    error(e.getMessage());
                }
                catch (BratAnnotationException e) {
                    error(e.getMessage());
                }
            }
        };
        // reset sentenceAddress and lastSentenceAddress to the orginal once

        mergeVisualizer.setOutputMarkupId(true);
        add(mergeVisualizer);

        textListView = new ListView<CurationViewForSourceDocument>("textListView",
                curationContainer.getCurationViews())
        {
            private static final long serialVersionUID = 8539162089561432091L;

            @Override
            protected void populateItem(ListItem<CurationViewForSourceDocument> item)
            {
                final CurationViewForSourceDocument curationViewItem = item.getModelObject();

                // ajax call when clicking on a sentence on the left side
                final AbstractDefaultAjaxBehavior click = new AbstractDefaultAjaxBehavior()
                {
                    private static final long serialVersionUID = 5803814168152098822L;

                    @Override
                    protected void respond(AjaxRequestTarget aTarget)
                    {
                        curationView = curationViewItem;
                        try {
                            CuratorUtil.updatePanel(aTarget, sentenceOuterView, curationContainer,
                                    mergeVisualizer, repository,
                                    annotationSelectionByUsernameAndAddress, curationView,
                                    annotationService, jsonConverter);

                            List<CurationViewForSourceDocument> views = curationContainer
                                    .getCurationViews();
                            for (CurationViewForSourceDocument segment : views) {
                                segment.setCurrentSentence(curationViewItem.getSentenceNumber()
                                        .equals(segment.getSentenceNumber()));
                            }

                            JCas jCas = null;
                            if (bratAnnotatorModel.getMode().equals(Mode.ANNOTATION)
                                    || bratAnnotatorModel.getMode().equals(Mode.CORRECTION)
                                    || bratAnnotatorModel.getMode().equals(Mode.CORRECTION_MERGE)) {

                                jCas = repository.readJCas(bratAnnotatorModel.getDocument(),
                                        bratAnnotatorModel.getProject(),
                                        bratAnnotatorModel.getUser());
                            }
                            else {
                                jCas = repository.getCurationDocumentContent(bratAnnotatorModel
                                        .getDocument());
                            }

                            if (bratAnnotatorModel.isScrollPage()) {
                                bratAnnotatorModel.setSentenceAddress(BratAjaxCasUtil
                                        .getSentenceBeginAddress(jCas,
                                                bratAnnotatorModel.getSentenceAddress(),
                                                curationViewItem.getBegin(),
                                                bratAnnotatorModel.getProject(),
                                                bratAnnotatorModel.getDocument(),
                                                bratAnnotatorModel.getWindowSize()));

                                Sentence sentence = selectByAddr(jCas, Sentence.class,
                                        bratAnnotatorModel.getSentenceAddress());
                                bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                                bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                                CurationBuilder builder = new CurationBuilder(repository,
                                        annotationService);
                                curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                                textListView.setModelObject(builder.buildCurationContainer(
                                        bratAnnotatorModel).getCurationViews());
                                onChange(aTarget);
                            }

                            // textListView.setModelObject(views);
                            textOuterView.addOrReplace(textListView);
                            aTarget.add(textOuterView);
                            aTarget.add(sentenceOuterView);

                            // Wicket-level rendering of annotator because it becomes visible
                            // after selecting a document
                            aTarget.add(mergeVisualizer);

                            // brat-level initialization and rendering of document
                            mergeVisualizer.bratInit(aTarget);
                            mergeVisualizer.bratRender(aTarget, jCas);

                        }
                        catch (UIMAException e) {
                            error(ExceptionUtils.getRootCause(e));
                        }
                        catch (ClassNotFoundException e) {
                            error(e.getMessage());
                        }
                        catch (IOException e) {
                            error(e.getMessage());
                        }
                        catch (BratAnnotationException e) {
                            error(e.getMessage());
                        }
                    }

                };

                // add subcomponents to the component
                item.add(click);
                String colorCode = curationViewItem.getSentenceState().getColorCode();

                if (colorCode != null) {
                    item.add(AttributeModifier.append("style", "background-color: " + colorCode
                            + ";"));
                }

                Label currentSentence = new AjaxLabel("sentence", curationViewItem.getText(), click);
                item.add(currentSentence);

                Label sentenceNumber = new AjaxLabel("sentenceNumber", curationViewItem
                        .getSentenceNumber().toString(), click);
                item.add(sentenceNumber);
            }

        };
        // add subcomponents to the component
        textListView.setOutputMarkupId(true);
        textOuterView.add(textListView);
    }

    protected void onChange(AjaxRequestTarget aTarget)
    {

    }

    @Override
    public void renderHead(IHeaderResponse response)
    {
        super.renderHead(response);

        if (firstLoad) {
            firstLoad = false;
        }
        else if (bratAnnotatorModel.getProject() != null) {
            // mergeVisualizer.setModelObject(bratAnnotatorModel);
            mergeVisualizer.setCollection("#" + bratAnnotatorModel.getProject().getName() + "/");
            mergeVisualizer.bratInitRenderLater(response);
        }
    }
}
