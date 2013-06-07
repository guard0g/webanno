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

package de.tudarmstadt.ukp.clarin.webanno.webapp.page.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.uima.UIMAException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationService;
import de.tudarmstadt.ukp.clarin.webanno.api.RepositoryService;
import de.tudarmstadt.ukp.clarin.webanno.model.Mode;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocumentState;
import de.tudarmstadt.ukp.clarin.webanno.model.User;
import de.tudarmstadt.ukp.clarin.webanno.tcf.TcfWriter;
import de.tudarmstadt.ukp.clarin.webanno.webapp.dao.DaoUtils;
import eu.clarin.weblicht.wlfxb.io.WLFormatException;

/**
 * A Panel used to add Project Guidelines in a selected {@link Project}
 *
 * @author Seid Muhie Yimam
 *
 */
public class ExportPanel
    extends Panel
{
    private static final long serialVersionUID = 2116717853865353733L;

    @SpringBean(name = "annotationService")
    private AnnotationService annotationService;

    @SpringBean(name = "documentRepository")
    private RepositoryService projectRepository;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ExportPanel(String id, final Model<Project> aProjectModel)
    {
        super(id);
       // final Project project = aProjectModel.getObject();

        add(new Button("send", new ResourceModel("label"))
        {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible()
            {
              return  projectRepository.isRemoteProject(aProjectModel.getObject());

            }

            @Override
            public void onSubmit()
            {

                HttpClient httpclient = new DefaultHttpClient();
                try {
                    HttpPost httppost = new HttpPost(
                            "http://aspra11.informatik.uni-leipzig.de:8080/"
                                    + "TEI-Integration/collection/addAnnotations?user=pws&pass=showcase");

                    File exportTempDir = File.createTempFile("webanno", "export");
                    exportTempDir.delete();
                    exportTempDir.mkdirs();

                    File metaInfDir = new File(exportTempDir + "/META_INF");
                    FileUtils.forceMkdir(metaInfDir);

                    boolean curationDocumentExist = isCurationDocumentExists(aProjectModel.getObject());

                    if (!curationDocumentExist) {
                        error("No curation document created yet for this document");
                    }
                    else {
                        copyCuratedDocuments(aProjectModel.getObject(), exportTempDir);
                        FileUtils.copyDirectory(new File(projectRepository.getDir(), "/project/"
                                + aProjectModel.getObject().getId() + "/META_INF"), metaInfDir);

                        DaoUtils.zipFolder(exportTempDir, new File(exportTempDir.getAbsolutePath()
                                + ".zip"));

                        @SuppressWarnings("deprecation")
                        FileEntity reqEntity = new FileEntity(new File(
                                exportTempDir.getAbsolutePath() + ".zip"),
                                "application/octet-stream");

                        httppost.setEntity(reqEntity);

                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity resEntity = response.getEntity();

                        info(response.getStatusLine().toString());
                        EntityUtils.consume(resEntity);
                    }
                }
                catch (ClientProtocolException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (IOException e) {
                    error(e.getMessage());
                }
                catch (UIMAException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (ClassNotFoundException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (WLFormatException e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                catch (Exception e) {
                    error(ExceptionUtils.getRootCause(e));
                }
                finally {
                    try {
                        httpclient.getConnectionManager().shutdown();
                    }
                    catch (Exception e) {
                        error(ExceptionUtils.getRootCause(e));
                    }
                }

            }
        }).setOutputMarkupId(true);

        add(new DownloadLink("export", new LoadableDetachableModel<File>()
        {
            private static final long serialVersionUID = 840863954694163375L;

            @Override
            protected File load()
            {
                File exportFile = null;
                try {
                    File exportTempDir = File.createTempFile("webanno", "export");
                    exportTempDir.delete();
                    exportTempDir.mkdirs();

                    boolean curationDocumentExist = isCurationDocumentExists(aProjectModel.getObject());

                    if (!curationDocumentExist) {
                        error("No curation document created yet for this document");
                    }
                    else {
                        copyCuratedDocuments(aProjectModel.getObject(), exportTempDir);
                        DaoUtils.zipFolder(exportTempDir, new File(exportTempDir.getAbsolutePath()
                                + ".zip"));
                        exportFile = new File(exportTempDir.getAbsolutePath() + ".zip");
                    }
                }
                catch (IOException e) {
                    error(e.getMessage());
                }
                catch (Exception e) {
                    error(e.getMessage());
                }

                return exportFile;
            }
        }){
            private static final long serialVersionUID = 5630612543039605914L;

            @Override
            public boolean isVisible()
            {
              return   isCurationDocumentExists(aProjectModel.getObject());

            }
        }).setOutputMarkupId(true);
    }

    /**
     * Copy, if exists, curation documents to a folder that will be exported as Zip file
     *
     * @param aProject
     *            The {@link Project}
     * @param aCurationDocumentExist
     *            Check if Curation document exists
     * @param aCopyDir
     *            The folder where curated documents are copied to be exported as Zip File
     */
    private void copyCuratedDocuments(Project aProject, File aCopyDir)
        throws FileNotFoundException, UIMAException, IOException, WLFormatException,
        ClassNotFoundException
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = projectRepository.getUser(username);

        // Get all the source documents from the project
        List<SourceDocument> documents = projectRepository.listSourceDocuments(aProject);

        for (SourceDocument sourceDocument : documents) {

            // If the curation document is exist (either finished or in progress
            if (sourceDocument.getState().equals(SourceDocumentState.CURATION_FINISHED)
                    || sourceDocument.getState().equals(SourceDocumentState.CURATION_IN_PROGRESS)) {
                File tcfFile = projectRepository.exportAnnotationDocument(sourceDocument, aProject,
                        user, TcfWriter.class, sourceDocument.getName(), Mode.CURATION);
                FileUtils.copyFileToDirectory(tcfFile, aCopyDir);
            }
        }
    }
    private boolean isCurationDocumentExists(Project aProject){
        boolean curationDocumentExist = false;
        List<SourceDocument> documents = projectRepository.listSourceDocuments(aProject);

        for (SourceDocument sourceDocument : documents) {

            // If the curation document is exist (either finished or in progress
            if (sourceDocument.getState().equals(SourceDocumentState.CURATION_FINISHED)
                    || sourceDocument.getState().equals(SourceDocumentState.CURATION_IN_PROGRESS)) {
                curationDocumentExist = true;
                break;
            }
        }
        return curationDocumentExist;
    }
}