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
package de.tudarmstadt.ukp.clarin.webanno.webapp.page.automation;

import static de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil.selectByAddr;
import static org.apache.uima.fit.util.JCasUtil.selectFollowing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.beans.BeansException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;

import wicket.contrib.input.events.EventType;
import wicket.contrib.input.events.InputBehavior;
import wicket.contrib.input.events.key.KeyType;
import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationService;
import de.tudarmstadt.ukp.clarin.webanno.api.RepositoryService;
import de.tudarmstadt.ukp.clarin.webanno.automation.util.AutomationUtil;
import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.BratAnnotator;
import de.tudarmstadt.ukp.clarin.webanno.brat.annotation.BratAnnotatorModel;
import de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAjaxCasUtil;
import de.tudarmstadt.ukp.clarin.webanno.brat.controller.BratAnnotationException;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.AnnotationSelection;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.CurationViewPanel;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationBuilder;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationContainer;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationUserSegmentForAnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.brat.curation.component.model.CurationViewForSourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.brat.project.ProjectUtil;
import de.tudarmstadt.ukp.clarin.webanno.brat.util.CuratorUtil;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocumentState;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;
import de.tudarmstadt.ukp.clarin.webanno.model.MiraTemplate;
import de.tudarmstadt.ukp.clarin.webanno.model.Mode;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.User;
import de.tudarmstadt.ukp.clarin.webanno.project.page.SettingsPageBase;
import de.tudarmstadt.ukp.clarin.webanno.webapp.dialog.OpenModalWindowPanel;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.AnnotationLayersModalPanel;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.DocumentNamePanel;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.ExportModalPanel;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.FinishImage;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.FinishLink;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.annotation.component.GuidelineModalPanel;
import de.tudarmstadt.ukp.clarin.webanno.webapp.page.welcome.WelcomePage;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * This is the main class for the Automation page. Displays in the lower panel the Automatically
 * annotated document and in the upper panel the annotation pane to trigger automation on the lower
 * pane.
 *
 * @author Seid Muhie Yimam
 */
public class AutomationPage
    extends SettingsPageBase
{    
    private static final Log LOG = LogFactory.getLog(AutomationPage.class);    

    private static final long serialVersionUID = 1378872465851908515L;

    @SpringBean(name = "jsonConverter")
    private MappingJacksonHttpMessageConverter jsonConverter;
    @SpringBean(name = "documentRepository")
    private RepositoryService repository;

    @SpringBean(name = "annotationService")
    private AnnotationService annotationService;

    private CurationContainer curationContainer;
    private BratAnnotatorModel bratAnnotatorModel;

    private Label numberOfPages;
    private DocumentNamePanel documentNamePanel;

    private int sentenceNumber = 1;
    private int totalNumberOfSentence;

    private long currentDocumentId;
    private long currentprojectId;

    // Open the dialog window on first load
    boolean firstLoad = true;

    private NumberTextField<Integer> gotoPageTextField;
    private int gotoPageAddress;

    private FinishImage finish;

    private CurationViewPanel automateView;
    private BratAnnotator mergeVisualizer;

    private final Map<String, Map<Integer, AnnotationSelection>> annotationSelectionByUsernameAndAddress = new HashMap<String, Map<Integer, AnnotationSelection>>();

    private final CurationViewForSourceDocument curationSegment = new CurationViewForSourceDocument();

    public AutomationPage()
    {
        bratAnnotatorModel = new BratAnnotatorModel();
        bratAnnotatorModel.setMode(Mode.AUTOMATION);

        LinkedList<CurationUserSegmentForAnnotationDocument> sentences = new LinkedList<CurationUserSegmentForAnnotationDocument>();
        CurationUserSegmentForAnnotationDocument curationUserSegmentForAnnotationDocument = new CurationUserSegmentForAnnotationDocument();
        if (bratAnnotatorModel.getDocument() != null) {
            curationUserSegmentForAnnotationDocument
                    .setAnnotationSelectionByUsernameAndAddress(annotationSelectionByUsernameAndAddress);
            curationUserSegmentForAnnotationDocument.setBratAnnotatorModel(bratAnnotatorModel);
            sentences.add(curationUserSegmentForAnnotationDocument);
        }
        automateView = new CurationViewPanel("automateView",
                new Model<LinkedList<CurationUserSegmentForAnnotationDocument>>(sentences))
        {
            private static final long serialVersionUID = 2583509126979792202L;

            @Override
            public void onChange(AjaxRequestTarget aTarget)
            {
                try {
                    // update begin/end of the curationsegment based on bratAnnotatorModel changes
                    // (like sentence change in auto-scroll mode,....
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
                    curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                    setCurationSegmentBeginEnd();

                    CuratorUtil.updatePanel(aTarget, this, curationContainer, mergeVisualizer,
                            repository, annotationSelectionByUsernameAndAddress, curationSegment,
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
                mergeVisualizer.bratRenderLater(aTarget);
                aTarget.add(numberOfPages);
                update(aTarget);
            }
        };

        automateView.setOutputMarkupId(true);
        add(automateView);

        mergeVisualizer = new BratAnnotator("mergeView", new Model<BratAnnotatorModel>(
                bratAnnotatorModel))
        {
            private static final long serialVersionUID = 7279648231521710155L;

            @Override
            protected void onChange(AjaxRequestTarget aTarget,
                    BratAnnotatorModel aBratAnnotatorModel)
            {
                try {
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
//                    info(bratAnnotatorModel.getMessage());
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
                    bratAnnotatorModel = aBratAnnotatorModel;
                    CurationBuilder builder = new CurationBuilder(repository);
                    curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                    setCurationSegmentBeginEnd();
                    curationContainer.setBratAnnotatorModel(bratAnnotatorModel);

                    CuratorUtil.updatePanel(aTarget, automateView, curationContainer, this,
                            repository, annotationSelectionByUsernameAndAddress, curationSegment,
                            annotationService, jsonConverter);
                    aTarget.add(automateView);
                    aTarget.add(numberOfPages);
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
                update(aTarget);
            }

            @Override
            protected void onAnnotate(BratAnnotatorModel aBratAnnotatorModel, int aStart, int aEnd)
            {
                MiraTemplate template;
                Set<AnnotationFeature> features = bratAnnotatorModel.getRememberedSpanFeatures()
                        .keySet();
                AnnotationFeature autoFeature = null;
                for (AnnotationFeature feature : features) {
                    autoFeature = feature;
                    break;
                }
                try {
                    template = repository.getMiraTemplate(autoFeature);
                    if (!template.isAnnotateAndPredict()) {
                        return;
                    }
                    /*
                     * Tag tag = annotationService.getTag(bratAnnotatorModel
                     * .getRememberedSpanFeatures().get(autoFeature), autoFeature.getTagset());
                     */
                    AutomationUtil.repeateAnnotation(bratAnnotatorModel, repository,
                            annotationService, aStart, aEnd, autoFeature);
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
                catch (NoResultException e) {// no automation layer is configured yet.
                    template = null;
                    return;
                }
            }

            @Override
            protected void onDelete(BratAnnotatorModel aBratAnnotatorModel, int aStart, int aEnd)
            {
                MiraTemplate template;
                Set<AnnotationFeature> features = bratAnnotatorModel.getRememberedSpanFeatures()
                        .keySet();
                AnnotationFeature autoFeature = null;
                for (AnnotationFeature feature : features) {
                    autoFeature = feature;
                    break;
                }
                if (autoFeature == null) {
                    return;
                }
                try {
                    template = repository.getMiraTemplate(autoFeature);
                    if (!template.isAnnotateAndPredict()) {
                        return;
                    }
                    /*
                     * Tag tag = annotationService.getTag(bratAnnotatorModel
                     * .getRememberedSpanFeatures().get(autoFeature), autoFeature.getTagset());
                     */
                    AutomationUtil.deleteAnnotation(bratAnnotatorModel, repository,
                            annotationService, aStart, aEnd, autoFeature);
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
                catch (NoResultException e) {// no automation layer is configured yet.
                    template = null;
                    return;
                }
            }
        };
        // reset sentenceAddress and lastSentenceAddress to the orginal once

        mergeVisualizer.setOutputMarkupId(true);
        add(mergeVisualizer);

        curationContainer = new CurationContainer();
        curationContainer.setBratAnnotatorModel(bratAnnotatorModel);

        add(documentNamePanel = new DocumentNamePanel("documentNamePanel",
                new Model<BratAnnotatorModel>(bratAnnotatorModel)));

        add(numberOfPages = (Label) new Label("numberOfPages",
                new LoadableDetachableModel<String>()
                {
                    private static final long serialVersionUID = 891566759811286173L;

                    @Override
                    protected String load()
                    {
                        if (bratAnnotatorModel.getDocument() != null) {

                            JCas mergeJCas = null;
                            try {

                                mergeJCas = repository
                                        .getCorrectionDocumentContent(bratAnnotatorModel
                                                .getDocument());

                                totalNumberOfSentence = BratAjaxCasUtil.getNumberOfPages(mergeJCas);

                                // If only one page, start displaying from sentence 1
                                /*
                                 * if (totalNumberOfSentence == 1) {
                                 * bratAnnotatorModel.setSentenceAddress(bratAnnotatorModel
                                 * .getFirstSentenceAddress()); }
                                 */
                                int address = BratAjaxCasUtil.selectSentenceAt(mergeJCas,
                                        bratAnnotatorModel.getSentenceBeginOffset(),
                                        bratAnnotatorModel.getSentenceEndOffset()).getAddress();
                                sentenceNumber = BratAjaxCasUtil.getFirstSentenceNumber(mergeJCas,
                                        address);
                                int firstSentenceNumber = sentenceNumber + 1;
                                int lastSentenceNumber;
                                if (firstSentenceNumber + bratAnnotatorModel.getWindowSize() - 1 < totalNumberOfSentence) {
                                    lastSentenceNumber = firstSentenceNumber
                                            + bratAnnotatorModel.getWindowSize() - 1;
                                }
                                else {
                                    lastSentenceNumber = totalNumberOfSentence;
                                }

                                return "showing " + firstSentenceNumber + "-" + lastSentenceNumber
                                        + " of " + totalNumberOfSentence + " sentences";
                            }
                            catch (UIMAException e) {
                                return "";
                            }
                            catch (DataRetrievalFailureException e) {
                                return "";
                            }
                            catch (ClassNotFoundException e) {
                                return "";
                            }
                            catch (FileNotFoundException e) {
                                return "";
                            }
                            catch (IOException e) {
                                return "";
                            }

                        }
                        else {
                            return "";// no document yet selected
                        }

                    }
                }).setOutputMarkupId(true));

        final ModalWindow openDocumentsModal;
        add(openDocumentsModal = new ModalWindow("openDocumentsModal"));
        openDocumentsModal.setOutputMarkupId(true);

        openDocumentsModal.setInitialWidth(500);
        openDocumentsModal.setInitialHeight(300);
        openDocumentsModal.setResizable(true);
        openDocumentsModal.setWidthUnit("px");
        openDocumentsModal.setHeightUnit("px");
        openDocumentsModal.setTitle("Open document");

        add(new AjaxLink<Void>("showOpenDocumentModal")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {
                openDocumentsModal.setContent(new OpenModalWindowPanel(openDocumentsModal
                        .getContentId(), bratAnnotatorModel, openDocumentsModal, Mode.AUTOMATION));
                openDocumentsModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
                {
                    private static final long serialVersionUID = -1746088901018629567L;

                    @Override
                    public void onClose(AjaxRequestTarget target)
                    {
                        if (bratAnnotatorModel.getDocument() == null) {
                            setResponsePage(WelcomePage.class);
                            return;
                        }

                        try {
                            target.addChildren(getPage(), FeedbackPanel.class);
                            bratAnnotatorModel.setDocument(bratAnnotatorModel.getDocument());
                            bratAnnotatorModel.setProject(bratAnnotatorModel.getProject());

                            String username = SecurityContextHolder.getContext()
                                    .getAuthentication().getName();

                            repository.upgradeCasAndSave(bratAnnotatorModel.getDocument(),
                                    Mode.AUTOMATION, username);
                            loadDocumentAction();
                            setCurationSegmentBeginEnd();
                            update(target);

                        }
                        catch (UIMAException e) {
                            target.addChildren(getPage(), FeedbackPanel.class);
                            error(ExceptionUtils.getRootCause(e));
                        }
                        catch (ClassNotFoundException e) {
                            target.addChildren(getPage(), FeedbackPanel.class);
                            error(e.getMessage());
                        }
                        catch (IOException e) {
                            target.addChildren(getPage(), FeedbackPanel.class);
                            error(e.getMessage());
                        }
                        catch (BratAnnotationException e) {
                            error(e.getMessage());
                        }
                        finish.setModelObject(bratAnnotatorModel);
                        target.add(finish.setOutputMarkupId(true));
                        target.appendJavaScript("Wicket.Window.unloadConfirmation=false;window.location.reload()");
                        target.add(documentNamePanel.setOutputMarkupId(true));
                        target.add(numberOfPages);
                    }
                });
                openDocumentsModal.show(aTarget);
            }
        });

        add(new AnnotationLayersModalPanel("annotationLayersModalPanel",
                new Model<BratAnnotatorModel>(bratAnnotatorModel))
        {
            private static final long serialVersionUID = -4657965743173979437L;

            @Override
            protected void onChange(AjaxRequestTarget aTarget)
            {
                curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                try {
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
                    setCurationSegmentBeginEnd();
                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCauseMessage(e));
                }
                catch (ClassNotFoundException e) {
                    error(e.getMessage());
                }
                catch (IOException e) {
                    error(e.getMessage());
                }
                update(aTarget);
                // mergeVisualizer.reloadContent(aTarget);
                aTarget.appendJavaScript("Wicket.Window.unloadConfirmation = false;window.location.reload()");

            }
        });

        add(new ExportModalPanel("exportModalPanel", new Model<BratAnnotatorModel>(
                bratAnnotatorModel)));

        gotoPageTextField = (NumberTextField<Integer>) new NumberTextField<Integer>("gotoPageText",
                new Model<Integer>(0));
        Form<Void> gotoPageTextFieldForm = new Form<Void>("gotoPageTextFieldForm");
        gotoPageTextFieldForm.add(new AjaxFormSubmitBehavior(gotoPageTextFieldForm, "onsubmit")
        {
            private static final long serialVersionUID = -4549805321484461545L;

            @Override
            protected void onSubmit(AjaxRequestTarget aTarget)
            {
                if (gotoPageAddress == 0) {
                    aTarget.appendJavaScript("alert('The sentence number entered is not valid')");
                    return;
                }
                JCas mergeJCas = null;
                try {
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
                    mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                            .getDocument());
                    if (bratAnnotatorModel.getSentenceAddress() != gotoPageAddress) {
                        bratAnnotatorModel.setSentenceAddress(gotoPageAddress);

                        Sentence sentence = selectByAddr(mergeJCas, Sentence.class, gotoPageAddress);
                        bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                        bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                        CurationBuilder builder = new CurationBuilder(repository);
                        curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                        setCurationSegmentBeginEnd();
                        curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                        update(aTarget);
                        mergeVisualizer.bratRenderLater(aTarget);
                    }
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
        });

        gotoPageTextField.setType(Integer.class);
        gotoPageTextField.setMinimum(1);
        gotoPageTextField.setDefaultModelObject(1);
        add(gotoPageTextFieldForm.add(gotoPageTextField));
        gotoPageTextField.add(new AjaxFormComponentUpdatingBehavior("onchange")
        {
            private static final long serialVersionUID = -3853194405966729661L;

            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                JCas mergeJCas = null;
                try {
                    mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                            .getDocument());
                    gotoPageAddress = BratAjaxCasUtil.getSentenceAddress(mergeJCas,
                            gotoPageTextField.getModelObject());
                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (ClassNotFoundException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (IOException e) {
                    error(e.getMessage());
                }

            }
        });

        add(new AjaxLink<Void>("gotoPageLink")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {

                if (gotoPageAddress == 0) {
                    aTarget.appendJavaScript("alert('The sentence number entered is not valid')");
                    return;
                }
                if (bratAnnotatorModel.getDocument() == null) {
                    aTarget.appendJavaScript("alert('Please open a document first!')");
                    return;
                }
                JCas mergeJCas = null;
                try {
                    aTarget.addChildren(getPage(), FeedbackPanel.class);
                    mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                            .getDocument());
                    if (bratAnnotatorModel.getSentenceAddress() != gotoPageAddress) {
                        bratAnnotatorModel.setSentenceAddress(gotoPageAddress);

                        Sentence sentence = selectByAddr(mergeJCas, Sentence.class, gotoPageAddress);
                        bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                        bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                        CurationBuilder builder = new CurationBuilder(repository);
                        curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                        setCurationSegmentBeginEnd();
                        curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                        update(aTarget);
                        mergeVisualizer.bratRenderLater(aTarget);
                    }
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
        });

        finish = new FinishImage("finishImage", new LoadableDetachableModel<BratAnnotatorModel>()
        {
            private static final long serialVersionUID = -2737326878793568454L;

            @Override
            protected BratAnnotatorModel load()
            {
                return bratAnnotatorModel;
            }
        });

        add(new FinishLink("showYesNoModalPanel",
                new Model<BratAnnotatorModel>(bratAnnotatorModel), finish)
        {
            private static final long serialVersionUID = -4657965743173979437L;
        });

        // Show the previous document, if exist
        add(new AjaxLink<Void>("showPreviousDocument")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            /**
             * Get the current beginning sentence address and add on it the size of the display
             * window
             */
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                target.addChildren(getPage(), FeedbackPanel.class);
                // List of all Source Documents in the project
                List<SourceDocument> listOfSourceDocuements = repository
                        .listSourceDocuments(bratAnnotatorModel.getProject());

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = repository.getUser(username);

                List<SourceDocument> sourceDocumentsinIgnorState = new ArrayList<SourceDocument>();
                for (SourceDocument sourceDocument : listOfSourceDocuements) {
                    if (repository.existsAnnotationDocument(sourceDocument, user)
                            && repository.getAnnotationDocument(sourceDocument, user).getState()
                                    .equals(AnnotationDocumentState.IGNORE)) {
                        sourceDocumentsinIgnorState.add(sourceDocument);
                    }
                    else if (sourceDocument.isTrainingDocument()) {
                        sourceDocumentsinIgnorState.add(sourceDocument);
                    }
                }

                listOfSourceDocuements.removeAll(sourceDocumentsinIgnorState);

                // Index of the current source document in the list
                int currentDocumentIndex = listOfSourceDocuements.indexOf(bratAnnotatorModel
                        .getDocument());

                // If the first the document
                if (currentDocumentIndex == 0) {
                    target.appendJavaScript("alert('This is the first document!')");
                }
                else {
                    bratAnnotatorModel.setDocumentName(listOfSourceDocuements.get(
                            currentDocumentIndex - 1).getName());
                    bratAnnotatorModel.setDocument(listOfSourceDocuements
                            .get(currentDocumentIndex - 1));

                    try {
                        repository.upgradeCasAndSave(bratAnnotatorModel.getDocument(),
                                Mode.AUTOMATION, bratAnnotatorModel.getUser().getUsername());
                        loadDocumentAction();
                        setCurationSegmentBeginEnd();
                        update(target);

                    }
                    catch (UIMAException e) {
                        error(ExceptionUtils.getRootCause(e));
                    }
                    catch (ClassNotFoundException e) {
                        error(ExceptionUtils.getRootCause(e));
                    }
                    catch (IOException e) {
                        error(ExceptionUtils.getRootCause(e));
                    }
                    catch (BratAnnotationException e) {
                        target.addChildren(getPage(), FeedbackPanel.class);
                        error(e.getMessage());
                    }

                    finish.setModelObject(bratAnnotatorModel);
                    target.add(finish.setOutputMarkupId(true));
                    target.add(numberOfPages);
                    target.add(documentNamePanel);
                    mergeVisualizer.bratRenderLater(target);
                }
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.Shift, KeyType.Page_up }, EventType.click)));

        // Show the next document if exist
        add(new AjaxLink<Void>("showNextDocument")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            /**
             * Get the current beginning sentence address and add on it the size of the display
             * window
             */
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                target.addChildren(getPage(), FeedbackPanel.class);
                // List of all Source Documents in the project
                List<SourceDocument> listOfSourceDocuements = repository
                        .listSourceDocuments(bratAnnotatorModel.getProject());

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = repository.getUser(username);

                List<SourceDocument> sourceDocumentsinIgnorState = new ArrayList<SourceDocument>();
                for (SourceDocument sourceDocument : listOfSourceDocuements) {
                    if (repository.existsAnnotationDocument(sourceDocument, user)
                            && repository.getAnnotationDocument(sourceDocument, user).getState()
                                    .equals(AnnotationDocumentState.IGNORE)) {
                        sourceDocumentsinIgnorState.add(sourceDocument);
                    }
                    else if (sourceDocument.isTrainingDocument()) {
                        sourceDocumentsinIgnorState.add(sourceDocument);
                    }
                }

                listOfSourceDocuements.removeAll(sourceDocumentsinIgnorState);

                // Index of the current source document in the list
                int currentDocumentIndex = listOfSourceDocuements.indexOf(bratAnnotatorModel
                        .getDocument());

                // If the first document
                if (currentDocumentIndex == listOfSourceDocuements.size() - 1) {
                    target.appendJavaScript("alert('This is the last document!')");
                    return;
                }
                bratAnnotatorModel.setDocumentName(listOfSourceDocuements.get(
                        currentDocumentIndex + 1).getName());
                bratAnnotatorModel
                        .setDocument(listOfSourceDocuements.get(currentDocumentIndex + 1));

                try {
                    repository.upgradeCasAndSave(bratAnnotatorModel.getDocument(), Mode.AUTOMATION,
                            bratAnnotatorModel.getUser().getUsername());
                    loadDocumentAction();
                    setCurationSegmentBeginEnd();
                    update(target);

                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (ClassNotFoundException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (IOException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (BratAnnotationException e) {
                    target.addChildren(getPage(), FeedbackPanel.class);
                    error(e.getMessage());
                }

                finish.setModelObject(bratAnnotatorModel);
                target.add(finish.setOutputMarkupId(true));
                target.add(numberOfPages);
                target.add(documentNamePanel);
                mergeVisualizer.bratRenderLater(target);
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.Shift, KeyType.Page_down }, EventType.click)));

        // Show the next page of this document
        add(new AjaxLink<Void>("showNext")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            /**
             * Get the current beginning sentence address and add on it the size of the display
             * window
             */
            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {
                if (bratAnnotatorModel.getDocument() != null) {
                    JCas mergeJCas = null;
                    try {
                        aTarget.addChildren(getPage(), FeedbackPanel.class);
                        mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                                .getDocument());
                        int address = BratAjaxCasUtil.selectSentenceAt(mergeJCas,
                                bratAnnotatorModel.getSentenceBeginOffset(),
                                bratAnnotatorModel.getSentenceEndOffset()).getAddress();
                        int nextSentenceAddress = BratAjaxCasUtil
                                .getNextDisplayWindowSentenceBeginAddress(mergeJCas, address,
                                        bratAnnotatorModel.getWindowSize());
                        if (address != nextSentenceAddress) {
                            bratAnnotatorModel.setSentenceAddress(nextSentenceAddress);

                            Sentence sentence = selectByAddr(mergeJCas, Sentence.class,
                                    nextSentenceAddress);
                            bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                            bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                            CurationBuilder builder = new CurationBuilder(repository);
                            curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                            setCurationSegmentBeginEnd();
                            curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                            update(aTarget);
                            mergeVisualizer.bratRenderLater(aTarget);
                        }

                        else {
                            aTarget.appendJavaScript("alert('This is last page!')");
                        }
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
                else {
                    aTarget.appendJavaScript("alert('Please open a document first!')");
                }
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.Page_down }, EventType.click)));

        // SHow the previous page of this document
        add(new AjaxLink<Void>("showPrevious")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {
                if (bratAnnotatorModel.getDocument() != null) {

                    JCas mergeJCas = null;
                    try {
                        aTarget.addChildren(getPage(), FeedbackPanel.class);
                        mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                                .getDocument());
                        int previousSentenceAddress = BratAjaxCasUtil
                                .getPreviousDisplayWindowSentenceBeginAddress(mergeJCas,
                                        bratAnnotatorModel.getSentenceAddress(),
                                        bratAnnotatorModel.getWindowSize());
                        if (bratAnnotatorModel.getSentenceAddress() != previousSentenceAddress) {
                            bratAnnotatorModel.setSentenceAddress(previousSentenceAddress);

                            Sentence sentence = selectByAddr(mergeJCas, Sentence.class,
                                    previousSentenceAddress);
                            bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                            bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                            CurationBuilder builder = new CurationBuilder(repository);

                            curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                            setCurationSegmentBeginEnd();
                            curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                            update(aTarget);
                            mergeVisualizer.bratRenderLater(aTarget);
                        }
                        else {
                            aTarget.appendJavaScript("alert('This is First Page!')");
                        }
                    }
                    catch (UIMAException e) {
                        error(ExceptionUtils.getRootCause(e));
                    }
                    catch (ClassNotFoundException e) {
                        error(e.getMessage());
                    }
                    catch (IOException e) {
                        ;
                        error(e.getMessage());
                    }
                    catch (BratAnnotationException e) {
                        error(e.getMessage());
                    }
                }
                else {
                    aTarget.appendJavaScript("alert('Please open a document first!')");
                }
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.Page_up }, EventType.click)));

        add(new AjaxLink<Void>("showFirst")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {
                if (bratAnnotatorModel.getDocument() != null) {
                    JCas mergeJCas = null;
                    try {
                        aTarget.addChildren(getPage(), FeedbackPanel.class);
                        mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                                .getDocument());

                        int address = BratAjaxCasUtil.selectSentenceAt(mergeJCas,
                                bratAnnotatorModel.getSentenceBeginOffset(),
                                bratAnnotatorModel.getSentenceEndOffset()).getAddress();
                        int firstAddress = BratAjaxCasUtil.getFirstSentenceAddress(mergeJCas);

                        if (firstAddress != address) {
                            bratAnnotatorModel.setSentenceAddress(firstAddress);

                            Sentence sentence = selectByAddr(mergeJCas, Sentence.class,
                                    firstAddress);
                            bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                            bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                            CurationBuilder builder = new CurationBuilder(repository);
                            curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                            setCurationSegmentBeginEnd();
                            curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                            update(aTarget);
                            mergeVisualizer.bratRenderLater(aTarget);
                        }
                        else {
                            aTarget.appendJavaScript("alert('This is first page!')");
                        }
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
                else {
                    aTarget.appendJavaScript("alert('Please open a document first!')");
                }
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.Home }, EventType.click)));

        add(new AjaxLink<Void>("showLast")
        {
            private static final long serialVersionUID = 7496156015186497496L;

            @Override
            public void onClick(AjaxRequestTarget aTarget)
            {
                if (bratAnnotatorModel.getDocument() != null) {
                    JCas mergeJCas = null;
                    try {
                        aTarget.addChildren(getPage(), FeedbackPanel.class);
                        mergeJCas = repository.getCorrectionDocumentContent(bratAnnotatorModel
                                .getDocument());
                        int lastDisplayWindowBeginingSentenceAddress = BratAjaxCasUtil
                                .getLastDisplayWindowFirstSentenceAddress(mergeJCas,
                                        bratAnnotatorModel.getWindowSize());
                        if (lastDisplayWindowBeginingSentenceAddress != bratAnnotatorModel
                                .getSentenceAddress()) {
                            bratAnnotatorModel
                                    .setSentenceAddress(lastDisplayWindowBeginingSentenceAddress);

                            Sentence sentence = selectByAddr(mergeJCas, Sentence.class,
                                    lastDisplayWindowBeginingSentenceAddress);
                            bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                            bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                            CurationBuilder builder = new CurationBuilder(repository);
                            curationContainer = builder.buildCurationContainer(bratAnnotatorModel);
                            setCurationSegmentBeginEnd();
                            curationContainer.setBratAnnotatorModel(bratAnnotatorModel);
                            update(aTarget);
                            mergeVisualizer.bratRenderLater(aTarget);

                        }
                        else {
                            aTarget.appendJavaScript("alert('This is last Page!')");
                        }
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
                else {
                    aTarget.appendJavaScript("alert('Please open a document first!')");
                }
            }
        }.add(new InputBehavior(new KeyType[] { KeyType.End }, EventType.click)));

        add(new GuidelineModalPanel("guidelineModalPanel", new Model<BratAnnotatorModel>(
                bratAnnotatorModel)));
    }

    /**
     * for the first time the page is accessed, open the <b>open document dialog</b>
     */
    @Override
    public void renderHead(IHeaderResponse response)
    {
        super.renderHead(response);

        String jQueryString = "";
        if (firstLoad) {
            jQueryString += "jQuery('#showOpenDocumentModal').trigger('click');";
            firstLoad = false;
        }
        response.render(OnLoadHeaderItem.forScript(jQueryString));
        if (bratAnnotatorModel.getProject() != null) {

            mergeVisualizer.setModelObject(bratAnnotatorModel);
            mergeVisualizer.setCollection("#" + bratAnnotatorModel.getProject().getName() + "/");
            mergeVisualizer.bratInitRenderLater(response);

        }

    }

    private void loadDocumentAction()
        throws UIMAException, ClassNotFoundException, IOException, BratAnnotationException
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User logedInUser = repository.getUser(SecurityContextHolder.getContext()
                .getAuthentication().getName());
        JCas jCas = null;
        try {
            AnnotationDocument logedInUserAnnotationDocument = repository.getAnnotationDocument(
                    bratAnnotatorModel.getDocument(), logedInUser);
            jCas = repository.getAnnotationDocumentContent(logedInUserAnnotationDocument);

        }
        catch (UIMAException e) {
            throw e;
        }
        catch (ClassNotFoundException e) {
            throw e;
        }
        // First time the Merge Cas is opened
        catch (IOException e) {
            throw e;
        }
        // Get information to be populated to bratAnnotatorModel from the JCAS of the logged in user
        //
        catch (DataRetrievalFailureException e) {

            jCas = repository.readJCas(bratAnnotatorModel.getDocument(), bratAnnotatorModel
                    .getDocument().getProject(), logedInUser);
            // This is the auto annotation, save it under CORRECTION_USER, Only if it is not created
            // by another annotater
            if (!repository.existsAutomatedDocument(bratAnnotatorModel.getDocument())) {
                repository.createCorrectionDocumentContent(jCas, bratAnnotatorModel.getDocument(),
                        logedInUser);
            }
        }
        catch (NoResultException e) {
            jCas = repository.readJCas(bratAnnotatorModel.getDocument(), bratAnnotatorModel
                    .getDocument().getProject(), logedInUser);
            // This is the auto annotation, save it under CORRECTION_USER, Only if it is not created
            // by another annotater
            if (!repository.existsAutomatedDocument(bratAnnotatorModel.getDocument())) {
                repository.createCorrectionDocumentContent(jCas, bratAnnotatorModel.getDocument(),
                        logedInUser);
            }
        }

        if (bratAnnotatorModel.getSentenceAddress() == -1
                || bratAnnotatorModel.getDocument().getId() != currentDocumentId
                || bratAnnotatorModel.getProject().getId() != currentprojectId) {

            try {
                bratAnnotatorModel
                        .setSentenceAddress(BratAjaxCasUtil.getFirstSentenceAddress(jCas));
                bratAnnotatorModel.setLastSentenceAddress(BratAjaxCasUtil
                        .getLastSentenceAddress(jCas));
                bratAnnotatorModel.setFirstSentenceAddress(bratAnnotatorModel.getSentenceAddress());

                Sentence sentence = selectByAddr(jCas, Sentence.class,
                        bratAnnotatorModel.getSentenceAddress());
                bratAnnotatorModel.setSentenceBeginOffset(sentence.getBegin());
                bratAnnotatorModel.setSentenceEndOffset(sentence.getEnd());

                ProjectUtil.setAnnotationPreference(username, repository, annotationService,
                        bratAnnotatorModel, Mode.AUTOMATION);
                
                LOG.debug("Configured BratAnnotatorModel for user [" + logedInUser.getUsername()
                        + "] f:[" + bratAnnotatorModel.getFirstSentenceAddress() + "] l:["
                        + bratAnnotatorModel.getLastSentenceAddress() + "] s:["
                        + bratAnnotatorModel.getSentenceAddress() + "]");
            }
            catch (DataRetrievalFailureException ex) {
                throw ex;
            }
            catch (BeansException e) {
                throw e;
            }
            catch (FileNotFoundException e) {
                throw e;
            }
            catch (IOException e) {
                throw e;
            }
        }
        bratAnnotatorModel.setUser(logedInUser);

        // if project is changed, reset some project specific settings
        if (currentprojectId != bratAnnotatorModel.getProject().getId()) {
            bratAnnotatorModel.setRememberedArcFeatures(null);
            bratAnnotatorModel.setRememberedArcLayer(null);
            bratAnnotatorModel.setRememberedSpanFeatures(null);
            bratAnnotatorModel.setRememberedSpanLayer(null);
//            bratAnnotatorModel.setMessage(null);
        }
        currentprojectId = bratAnnotatorModel.getProject().getId();
        currentDocumentId = bratAnnotatorModel.getDocument().getId();
    }

    private void setCurationSegmentBeginEnd()
        throws UIMAException, ClassNotFoundException, IOException
    {
        JCas jCas = repository.readJCas(bratAnnotatorModel.getDocument(),
                bratAnnotatorModel.getProject(), bratAnnotatorModel.getUser());

        final int sentenceAddress = BratAjaxCasUtil.selectSentenceAt(jCas,
                bratAnnotatorModel.getSentenceBeginOffset(),
                bratAnnotatorModel.getSentenceEndOffset()).getAddress();

        final Sentence sentence = selectByAddr(jCas, Sentence.class, sentenceAddress);
        List<Sentence> followingSentences = selectFollowing(jCas, Sentence.class, sentence,
                bratAnnotatorModel.getWindowSize());
        // Check also, when getting the last sentence address in the display window, if this is the
        // last sentence or the ONLY sentence in the document
        Sentence lastSentenceAddressInDisplayWindow = followingSentences.size() == 0 ? sentence
                : followingSentences.get(followingSentences.size() - 1);
        curationSegment.setBegin(sentence.getBegin());
        curationSegment.setEnd(lastSentenceAddressInDisplayWindow.getEnd());

    }

    private void update(AjaxRequestTarget target)
    {
        JCas correctionDocument = null;
        try {
            correctionDocument = CuratorUtil.updatePanel(target, automateView, curationContainer,
                    mergeVisualizer, repository, annotationSelectionByUsernameAndAddress,
                    curationSegment, annotationService, jsonConverter);
        }
        catch (UIMAException e) {
            error(ExceptionUtils.getRootCauseMessage(e));
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
        
        gotoPageTextField.setModelObject(BratAjaxCasUtil.getFirstSentenceNumber(correctionDocument,
                bratAnnotatorModel.getSentenceAddress()) + 1);
        gotoPageAddress = BratAjaxCasUtil.getSentenceAddress(correctionDocument,
                gotoPageTextField.getModelObject());
        
        target.add(gotoPageTextField);
        target.add(automateView);
        target.add(numberOfPages);
    }
}
