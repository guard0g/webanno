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
package de.tudarmstadt.ukp.clarin.webanno.api.dao;

import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.CHAIN_TYPE;
import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.CORRECTION_USER;
import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.CURATION_USER;
import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.INITIAL_CAS_PSEUDO_USER;
import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.RELATION_TYPE;
import static de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst.SPAN_TYPE;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.uima.cas.impl.Serialization.deserializeCASComplete;
import static org.apache.uima.cas.impl.Serialization.serializeCASComplete;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.Serialization;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.apache.uima.util.CasCreationUtils;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import de.tudarmstadt.ukp.clarin.webanno.api.AnnotationService;
import de.tudarmstadt.ukp.clarin.webanno.api.RepositoryService;
import de.tudarmstadt.ukp.clarin.webanno.api.UserDao;
import de.tudarmstadt.ukp.clarin.webanno.api.WebAnnoConst;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocumentState;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationFeature;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationLayer;
import de.tudarmstadt.ukp.clarin.webanno.model.Authority;
import de.tudarmstadt.ukp.clarin.webanno.model.AutomationStatus;
import de.tudarmstadt.ukp.clarin.webanno.model.CrowdJob;
import de.tudarmstadt.ukp.clarin.webanno.model.MiraTemplate;
import de.tudarmstadt.ukp.clarin.webanno.model.Mode;
import de.tudarmstadt.ukp.clarin.webanno.model.PermissionLevel;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.model.ProjectPermission;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocumentStateTransition;
import de.tudarmstadt.ukp.clarin.webanno.model.TagSet;
import de.tudarmstadt.ukp.clarin.webanno.model.User;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * Implementation of methods defined in the {@link RepositoryService} interface
 *
 * @author Seid Muhie Yimam
 *
 */
public class RepositoryServiceDbData
    implements RepositoryService
{
    private final Log log = LogFactory.getLog(getClass());
    
    public static Logger createLog(Project aProject, String aUser)
        throws IOException
    {
        Logger logger = Logger.getLogger(RepositoryServiceDbData.class);
        String targetLog = dir.getAbsolutePath() + PROJECT + "project-" + aProject.getId() + ".log";
        FileAppender apndr = new FileAppender(new PatternLayout("%d [" + aUser + "] %m%n"),
                targetLog, true);
        logger.addAppender(apndr);
        logger.setLevel(Level.ALL);
        return logger;
    }

    @Resource(name = "annotationService")
    private AnnotationService annotationService;

    @Resource(name = "userRepository")
    private UserDao userRepository;

    @Value(value = "${backup.keep.time}")
    private long backupKeepTime;

    @Value(value = "${crowdsource.enabled}")
    private int crowdsourceEnabled;

    @Value(value = "${backup.interval}")
    private long backupInterval;

    @Value(value = "${backup.keep.number}")
    private int backupKeepNumber;

    @Resource(name = "formats")
    private Properties readWriteFileFormats;

    @Resource(name = "helpFile")
    private Properties helpProperiesFile;

    private static final String PROJECT = "/project/";
    private static final String MIRA = "/mira/";
    private static final String MIRA_TEMPLATE = "/template/";
    private static final String DOCUMENT = "/document/";
    private static final String SOURCE = "/source";
    private static final String GUIDELINE = "/guideline/";
    private static final String ANNOTATION = "/annotation";
    private static final String SETTINGS = "/settings/";
    private static final String META_INF = "/META-INF/";

    private static final String TEMPLATE = "/crowdtemplates/";

    private static final String HELP_FILE = "/help.properties";

    @PersistenceContext
    private EntityManager entityManager;

    private static File dir;

    // The annotation preference properties File name
    String annotationPreferencePropertiesFileName;

    private final Object lock = new Object();

    public RepositoryServiceDbData()
    {

    }

    @Override
    @Transactional
    public void createAnnotationDocument(AnnotationDocument aAnnotationDocument)
        throws IOException
    {
        if (aAnnotationDocument.getId() == 0) {
            entityManager.persist(aAnnotationDocument);
        }
        else {
            entityManager.merge(aAnnotationDocument);
        }

        createLog(aAnnotationDocument.getProject(), aAnnotationDocument.getUser()).info(
                " User [" + aAnnotationDocument.getUser()
                        + "] creates annotation document for source document ["
                        + aAnnotationDocument.getDocument().getId() + "] in project ["
                        + aAnnotationDocument.getProject().getId() + "] with id ["
                        + aAnnotationDocument.getId() + "]");
        createLog(aAnnotationDocument.getProject(), aAnnotationDocument.getUser())
                .removeAllAppenders();
    }

    /**
     * Renames a file.
     *
     * @throws IOException
     *             if the file cannot be renamed.
     * @return the target file.
     */
    private File renameFile(File aFrom, File aTo)
        throws IOException
    {
        if (!aFrom.renameTo(aTo)) {
            throw new IOException("Cannot renamed file [" + aFrom + "] to [" + aTo + "]");
        }

        // We are not sure if File is mutable. This makes sure we get a new file
        // in any case.
        return new File(aTo.getPath());
    }

    /**
     * Get the folder where the annotations are stored. Creates the folder if necessary.
     *
     * @throws IOException
     *             if the folder cannot be created.
     */
    private File getAnnotationFolder(SourceDocument aDocument)
        throws IOException
    {
        File annotationFolder = new File(dir, PROJECT + aDocument.getProject().getId() + DOCUMENT
                + aDocument.getId() + ANNOTATION);
        FileUtils.forceMkdir(annotationFolder);
        return annotationFolder;
    }

    @Override
    public File getDocumentFolder(SourceDocument aDocument)
        throws IOException
    {
        File sourceDocFolder = new File(dir, PROJECT + aDocument.getProject().getId() + DOCUMENT
                + aDocument.getId() + SOURCE);
        FileUtils.forceMkdir(sourceDocFolder);
        return sourceDocFolder;
    }

    @Override
    @Transactional
    public void writeAnnotationCas(JCas aJcas, SourceDocument aDocument, User aUser)
        throws IOException
    {
        writeCas(aDocument, aJcas, aUser.getUsername(), aUser);
    }

    @Override
    @Transactional
    public void createProject(Project aProject, User aUser)
        throws IOException
    {
        entityManager.persist(aProject);
        String path = dir.getAbsolutePath() + PROJECT + aProject.getId();
        FileUtils.forceMkdir(new File(path));
        createLog(aProject, aUser.getUsername())
                .info(" Created  Project [" + aProject.getName() + "] with ID [" + aProject.getId()
                        + "]");
        createLog(aProject, aUser.getUsername()).removeAllAppenders();
    }

    @Override
    @Transactional
    public void createCrowdJob(CrowdJob aCrowdJob)
        throws IOException
    {
        if (aCrowdJob.getId() == 0) {
            entityManager.persist(aCrowdJob);
        }
        else {
            entityManager.merge(aCrowdJob);
        }

        createLog(aCrowdJob.getProject(), "crowd_user").info(
                " Created  crowd job from project [" + aCrowdJob.getProject() + "] with ID ["
                        + aCrowdJob.getId() + "]");
        createLog(aCrowdJob.getProject(), "crowd_user").removeAllAppenders();
    }

    @Override
    @Transactional
    public void createProjectPermission(ProjectPermission aPermission)
        throws IOException
    {
        entityManager.persist(aPermission);
        createLog(aPermission.getProject(), aPermission.getUser()).info(
                " New Permission created on Project[" + aPermission.getProject().getName()
                        + "] for user [" + aPermission.getUser() + "] with permission ["
                        + aPermission.getLevel() + "]" + "]");
        createLog(aPermission.getProject(), aPermission.getUser()).removeAllAppenders();
    }

    @Override
    @Transactional
    public void createSourceDocument(SourceDocument aDocument, User aUser)
        throws IOException
    {
        if (aDocument.getId() == 0) {
            entityManager.persist(aDocument);
        }
        else {
            entityManager.merge(aDocument);
        }

    }

    @Override
    @Transactional
    public boolean existsAnnotationDocument(SourceDocument aDocument, User aUser)
    {
        try {
            entityManager
                    .createQuery(
                            "FROM AnnotationDocument WHERE project = :project "
                                    + " AND document = :document AND user = :user",
                            AnnotationDocument.class)
                    .setParameter("project", aDocument.getProject())
                    .setParameter("document", aDocument).setParameter("user", aUser.getUsername())
                    .getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean existsCorrectionDocument(SourceDocument aDocument)
    {

        try {
            readCorrectionCas(aDocument);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean existsProject(String aName)
    {
        try {
            entityManager.createQuery("FROM Project WHERE name = :name", Project.class)
                    .setParameter("name", aName).getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean existsCas(SourceDocument aSourceDocument, String aUsername)
        throws IOException
    {
        return new File(getAnnotationFolder(aSourceDocument), aUsername + ".ser").exists();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public boolean existsCorrectionCas(SourceDocument aSourceDocument)
    {

        try {
            readCorrectionCas(aSourceDocument);
            return true;
        }
        catch (UIMAException e) {
            return false;
        }
        catch (DataRetrievalFailureException e) {
            return false;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }

    }

    @Override
    @Transactional
    public boolean existsCrowdJob(String aName)
    {
        try {
            entityManager.createQuery("FROM CrowdJob WHERE name = :name", CrowdJob.class)
                    .setParameter("name", aName).getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public boolean existsProjectPermission(User aUser, Project aProject)
    {

        List<ProjectPermission> projectPermissions = entityManager
                .createQuery(
                        "FROM ProjectPermission WHERE user = :user AND " + "project =:project",
                        ProjectPermission.class).setParameter("user", aUser.getUsername())
                .setParameter("project", aProject).getResultList();
        // if at least one permission level exist
        if (projectPermissions.size() > 0) {
            return true;
        }
        else {
            return false;
        }

    }

    @Override
    @Transactional
    public boolean existsProjectPermissionLevel(User aUser, Project aProject, PermissionLevel aLevel)
    {
        try {
            entityManager
                    .createQuery(
                            "FROM ProjectPermission WHERE user = :user AND "
                                    + "project =:project AND level =:level",
                            ProjectPermission.class).setParameter("user", aUser.getUsername())
                    .setParameter("project", aProject).setParameter("level", aLevel)
                    .getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean existsSourceDocument(Project aProject, String aFileName)
    {
        try {
            entityManager
                    .createQuery(
                            "FROM SourceDocument WHERE project = :project AND " + "name =:name ",
                            SourceDocument.class).setParameter("project", aProject)
                    .setParameter("name", aFileName).getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean existsProjectTimeStamp(Project aProject, String aUsername)
    {
        try {

            if (getProjectTimeStamp(aProject, aUsername) == null) {
                return false;
            }
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public boolean existsProjectTimeStamp(Project aProject)
    {
        try {

            if (getProjectTimeStamp(aProject) == null) {
                return false;
            }
            return true;
        }
        catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public boolean existsUser(String username)
    {

        try {
            getUser(username);
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
    }

    /**
     * A new directory is created using UUID so that every exported file will reside in its own
     * directory. This is useful as the written file can have multiple extensions based on the
     * Writer class used.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @Transactional
    public File exportAnnotationDocument(SourceDocument aDocument, String aUser, Class aWriter,
            String aFileName, Mode aMode)
        throws UIMAException, IOException, ClassNotFoundException
    {
        return exportAnnotationDocument(aDocument, aUser, aWriter, aFileName, aMode, true);
    }

    /**
     * A new directory is created using UUID so that every exported file will reside in its own
     * directory. This is useful as the written file can have multiple extensions based on the
     * Writer class used.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @Transactional
    public File exportAnnotationDocument(SourceDocument aDocument, String aUser, Class aWriter,
            String aFileName, Mode aMode, boolean aStripExtension)
        throws UIMAException, IOException, ClassNotFoundException
    {
        File annotationFolder = getAnnotationFolder(aDocument);
        String serializedCasFileName;
        // for Correction, it will export the corrected document (of the logged in user)
        // (CORRECTION_USER.ser is the automated result displayed for the user to correct it, not 
        // the final result) for automation, it will export either the corrected document
        // (Annotated) or the automated document
        if (aMode.equals(Mode.ANNOTATION) || aMode.equals(Mode.AUTOMATION)
                || aMode.equals(Mode.CORRECTION)) {
            serializedCasFileName = aUser + ".ser";
        }
        // The merge result will be exported
        else {
            serializedCasFileName = WebAnnoConst.CURATION_USER + ".ser";
        }

        // Read file
        File serializedCasFile = new File(annotationFolder, serializedCasFileName);
        if (!serializedCasFile.exists()) {
            throw new FileNotFoundException("CAS file [" + serializedCasFileName
                    + "] not found in [" + annotationFolder + "]");
        }
        
        CAS cas = CasCreationUtils.createCas((TypeSystemDescription) null, null, null);
        readSerializedCas(cas.getJCas(), serializedCasFile);
        
        // Update type system the CAS
        upgradeCas(cas, aDocument, aUser, aMode);
        
        // Update the source file name in case it is changed for some reason
        Project project = aDocument.getProject();
        File currentDocumentUri = new File(dir.getAbsolutePath() + PROJECT + project.getId()
                + DOCUMENT + aDocument.getId() + SOURCE);
        DocumentMetaData documentMetadata = DocumentMetaData.get(cas.getJCas());
        documentMetadata.setDocumentUri(new File(currentDocumentUri, aFileName).toURI().toURL()
                .toExternalForm());
        documentMetadata.setDocumentBaseUri(currentDocumentUri.toURI().toURL().toExternalForm());
        documentMetadata.setCollectionId(currentDocumentUri.toURI().toURL().toExternalForm());
        documentMetadata.setDocumentUri(new File(dir.getAbsolutePath() + PROJECT + project.getId()
                + DOCUMENT + aDocument.getId() + SOURCE + "/" + aFileName).toURI().toURL()
                .toExternalForm());
        
        // update with the correct tagset name
        List<AnnotationFeature> features = annotationService.listAnnotationFeature(project);
        for (AnnotationFeature feature : features) {

            TagSet tagSet = feature.getTagset();
            if (tagSet == null) {
                continue;
            }
            else if (!feature.getLayer().getType().equals(WebAnnoConst.CHAIN_TYPE)) {
                updateCasWithTagSet(cas, feature.getLayer().getName(), tagSet.getName());
            }
        }

        File exportTempDir = File.createTempFile("webanno", "export");
        exportTempDir.delete();
        exportTempDir.mkdirs();
        
        AnalysisEngineDescription writer;
        if (aWriter.getName()
                .equals("de.tudarmstadt.ukp.clarin.webanno.tsv.WebannoCustomTsvWriter")) {
            List<AnnotationLayer> layers = annotationService
                    .listAnnotationLayer(aDocument.getProject());
            List<String> multipleSpans = new ArrayList<String>();
            for (AnnotationLayer layer : layers) {
                if (layer.isMultipleTokens()) {
                    multipleSpans.add(layer.getName());
                }
            }

            writer = createEngineDescription(aWriter,
                    JCasFileWriter_ImplBase.PARAM_TARGET_LOCATION, exportTempDir,
                    JCasFileWriter_ImplBase.PARAM_STRIP_EXTENSION, aStripExtension, 
                    "multipleSpans", multipleSpans);
        }
        else {
            writer = createEngineDescription(aWriter,
                    JCasFileWriter_ImplBase.PARAM_TARGET_LOCATION, exportTempDir,
                    JCasFileWriter_ImplBase.PARAM_STRIP_EXTENSION, aStripExtension);
        }
        
        runPipeline(cas, writer);

        createLog(project, aUser).info(
                " Exported annotation file [" + aDocument.getName() + "] with ID ["
                        + aDocument.getId() + "] for user [" + aUser + "] from project ["
                        + project.getId() + "]");
        createLog(project, aUser).removeAllAppenders();

        File exportFile;
        if (exportTempDir.listFiles().length > 1) {
            exportFile = new File(exportTempDir.getAbsolutePath() + ".zip");
            try {
                ZipUtils.zipFolder(exportTempDir, exportFile);
            }
            catch (Exception e) {
                createLog(project, aUser).info("Unable to create zip File");
            }
        }
        else {
            exportFile = new File(exportTempDir.getParent(), exportTempDir.listFiles()[0].getName());
            FileUtils.copyFile(exportTempDir.listFiles()[0], exportFile);
        }
        FileUtils.forceDelete(exportTempDir);
        return exportFile;
    }

    @Override
    public File getSourceDocumentFile(SourceDocument aDocument)
    {
        File documentUri = new File(dir.getAbsolutePath() + PROJECT
                + aDocument.getProject().getId() + DOCUMENT + aDocument.getId() + SOURCE);
        return new File(documentUri, aDocument.getName());
    }

    @Override
    public File getCasFile(SourceDocument aDocument, String aUser)
    {
        File documentUri = new File(dir.getAbsolutePath() + PROJECT
                + aDocument.getProject().getId() + DOCUMENT + aDocument.getId() + ANNOTATION);
        return new File(documentUri, aUser + ".ser");
    }

    @Override
    public File getProjectLogFile(Project aProject)
    {
        return new File(dir.getAbsolutePath() + PROJECT + "project-" + aProject.getId() + ".log");
    }

    @Override
    public File getGuidelinesFile(Project aProject)
    {
        return new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + GUIDELINE);
    }

    @Override
    public File getMetaInfFolder(Project aProject)
    {
        return new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + META_INF);
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public AnnotationDocument getAnnotationDocument(SourceDocument aDocument, User aUser)
    {
        return entityManager
                .createQuery(
                        "FROM AnnotationDocument WHERE document = :document AND " + "user =:user"
                                + " AND project = :project", AnnotationDocument.class)
                .setParameter("document", aDocument).setParameter("user", aUser.getUsername())
                .setParameter("project", aDocument.getProject()).getSingleResult();
    }

    @Override
    @Transactional
    public JCas readAnnotationCas(AnnotationDocument aAnnotationDocument)
        throws IOException, UIMAException, ClassNotFoundException
    {
        return readCas(aAnnotationDocument.getDocument(), aAnnotationDocument.getUser());
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<Authority> listAuthorities(User aUser)
    {
        return entityManager
                .createQuery("FROM Authority where username =:username", Authority.class)
                .setParameter("username", aUser).getResultList();
    }

    @Override
    public File getDir()
    {
        return dir;
    }

    @Override
    public File getGuideline(Project aProject, String aFilename)
    {
        return new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + GUIDELINE + aFilename);
    }

    @Override
    public File getTemplate(String fileName)
        throws IOException
    {
        FileUtils.forceMkdir(new File(dir.getAbsolutePath() + TEMPLATE));
        return new File(dir.getAbsolutePath() + TEMPLATE, fileName);
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<ProjectPermission> listProjectPermisionLevel(User aUser, Project aProject)
    {
        return entityManager
                .createQuery("FROM ProjectPermission WHERE user =:user AND " + "project =:project",
                        ProjectPermission.class).setParameter("user", aUser.getUsername())
                .setParameter("project", aProject).getResultList();
    }

    @Override
    public List<User> listProjectUsersWithPermissions(Project aProject)
    {

        List<String> usernames = entityManager
                .createQuery(
                        "SELECT DISTINCT user FROM ProjectPermission WHERE "
                                + "project =:project ORDER BY user ASC", String.class)
                .setParameter("project", aProject).getResultList();

        List<User> users = new ArrayList<User>();

        for (String username : usernames) {
            if (existsUser(username)) {
                users.add(getUser(username));
            }
        }
        return users;
    }

    @Override
    public List<User> listProjectUsersWithPermissions(Project aProject,
            PermissionLevel aPermissionLevel)
    {
        List<String> usernames = entityManager
                .createQuery(
                        "SELECT DISTINCT user FROM ProjectPermission WHERE "
                                + "project =:project AND level =:level ORDER BY user ASC",
                        String.class).setParameter("project", aProject)
                .setParameter("level", aPermissionLevel).getResultList();
        List<User> users = new ArrayList<User>();
        for (String username : usernames) {
            if (existsUser(username)) {
                users.add(getUser(username));
            }
        }
        return users;
    }

    @Override
    @Transactional
    public Project getProject(String aName)
    {
        return entityManager.createQuery("FROM Project WHERE name = :name", Project.class)
                .setParameter("name", aName).getSingleResult();
    }

    @Override
    @Transactional
    public CrowdJob getCrowdJob(String aName, Project aProjec)
    {
        return entityManager
                .createQuery("FROM CrowdJob WHERE name = :name AND project = :project",
                        CrowdJob.class).setParameter("name", aName)
                .setParameter("project", aProjec).getSingleResult();
    }

    @Override
    public Project getProject(long aId)
    {
        return entityManager.createQuery("FROM Project WHERE id = :id", Project.class)
                .setParameter("id", aId).getSingleResult();
    }

    @Override
    public void createGuideline(Project aProject, File aContent, String aFileName, String aUsername)
        throws IOException
    {
        String guidelinePath = dir.getAbsolutePath() + PROJECT + aProject.getId() + GUIDELINE;
        FileUtils.forceMkdir(new File(guidelinePath));
        copyLarge(new FileInputStream(aContent), new FileOutputStream(new File(guidelinePath
                + aFileName)));

        createLog(aProject, aUsername).info(
                " Created Guideline file[ " + aFileName + "] for Project [" + aProject.getName()
                        + "] with ID [" + aProject.getId() + "]");
        createLog(aProject, aUsername).removeAllAppenders();
    }

    @Override
    public void createTemplate(Project aProject, File aContent, String aFileName, String aUsername)
        throws IOException
    {
        String templatePath = dir.getAbsolutePath() + PROJECT + aProject.getId() + MIRA
                + MIRA_TEMPLATE;
        FileUtils.forceMkdir(new File(templatePath));
        copyLarge(new FileInputStream(aContent), new FileOutputStream(new File(templatePath
                + aFileName)));

        createLog(aProject, aUsername).info(
                " Created Template file[ " + aFileName + "] for Project [" + aProject.getName()
                        + "] with ID [" + aProject.getId() + "]");
        createLog(aProject, aUsername).removeAllAppenders();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<ProjectPermission> getProjectPermisions(Project aProject)
    {
        return entityManager
                .createQuery("FROM ProjectPermission WHERE project =:project",
                        ProjectPermission.class).setParameter("project", aProject).getResultList();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public SourceDocument getSourceDocument(Project aProject, String aDocumentName)
    {

        return entityManager
                .createQuery("FROM SourceDocument WHERE name = :name AND project =:project",
                        SourceDocument.class).setParameter("name", aDocumentName)
                .setParameter("project", aProject).getSingleResult();
    }

    @Override
    @Transactional
    public Date getProjectTimeStamp(Project aProject, String aUsername)
    {
        return entityManager
                .createQuery(
                        "SELECT max(timestamp) FROM AnnotationDocument WHERE project = :project "
                                + " AND user = :user", Date.class)
                .setParameter("project", aProject).setParameter("user", aUsername)
                .getSingleResult();
    }

    @Override
    public Date getProjectTimeStamp(Project aProject)
    {
        return entityManager
                .createQuery("SELECT max(timestamp) FROM SourceDocument WHERE project = :project",
                        Date.class).setParameter("project", aProject).getSingleResult();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public User getUser(String aUsername)
    {
        return entityManager.createQuery("FROM User WHERE username =:username", User.class)
                .setParameter("username", aUsername).getSingleResult();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public boolean existsFinishedAnnotation(SourceDocument aDocument)
    {
        List<AnnotationDocument> annotationDocuments = entityManager
                .createQuery("FROM AnnotationDocument WHERE document = :document",
                        AnnotationDocument.class).setParameter("document", aDocument)
                .getResultList();
        for (AnnotationDocument annotationDocument : annotationDocuments) {
            if (annotationDocument.getState().equals(AnnotationDocumentState.FINISHED)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public boolean isAnnotationFinished(SourceDocument aDocument, User aUser)
    {
        try {
            AnnotationDocument annotationDocument = entityManager
                    .createQuery(
                            "FROM AnnotationDocument WHERE document = :document AND "
                                    + "user =:user", AnnotationDocument.class)
                    .setParameter("document", aDocument).setParameter("user", aUser.getUsername())
                    .getSingleResult();
            if (annotationDocument.getState().equals(AnnotationDocumentState.FINISHED)) {
                return true;
            }
            else {
                return false;
            }
        }
        // User even didn't start annotating
        catch (NoResultException e) {
            return false;
        }
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<AnnotationDocument> listAnnotationDocuments(SourceDocument aDocument)
    {
        // Get all annotators in the project
        List<String> users = getAllAnnotators(aDocument.getProject());
        // Bail out already. HQL doesn't seem to like queries with an empty
        // parameter right of "in"
        if (users.isEmpty()) {
            return new ArrayList<AnnotationDocument>();
        }

        return entityManager
                .createQuery(
                        "FROM AnnotationDocument WHERE project = :project AND document = :document "
                                + "AND user in (:users)", AnnotationDocument.class)
                .setParameter("project", aDocument.getProject()).setParameter("users", users)
                .setParameter("document", aDocument).getResultList();
    }

    @Override
    public int numberOfExpectedAnnotationDocuments(Project aProject)
    {

        // Get all annotators in the project
        List<String> users = getAllAnnotators(aProject);
        // Bail out already. HQL doesn't seem to like queries with an empty
        // parameter right of "in"
        if (users.isEmpty()) {
            return 0;
        }

        int ignored = 0;
        List<AnnotationDocument> annotationDocuments = entityManager
                .createQuery(
                        "FROM AnnotationDocument WHERE project = :project AND user in (:users)",
                        AnnotationDocument.class).setParameter("project", aProject)
                .setParameter("users", users).getResultList();
        for (AnnotationDocument annotationDocument : annotationDocuments) {
            if (annotationDocument.getState().equals(AnnotationDocumentState.IGNORE)) {
                ignored++;
            }
        }
        return listSourceDocuments(aProject).size() * users.size() - ignored;

    }

    @Override
    public List<AnnotationDocument> listFinishedAnnotationDocuments(Project aProject)
    {
        // Get all annotators in the project
        List<String> users = getAllAnnotators(aProject);
        // Bail out already. HQL doesn't seem to like queries with an empty
        // parameter right of "in"
        if (users.isEmpty()) {
            return new ArrayList<AnnotationDocument>();
        }

        return entityManager
                .createQuery(
                        "FROM AnnotationDocument WHERE project = :project AND state = :state"
                                + " AND user in (:users)", AnnotationDocument.class)
                .setParameter("project", aProject).setParameter("users", users)
                .setParameter("state", AnnotationDocumentState.FINISHED).getResultList();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<AnnotationDocument> listAllAnnotationDocuments(SourceDocument aSourceDocument)
    {
        return entityManager
                .createQuery(
                        "FROM AnnotationDocument WHERE project = :project AND document = :document",
                        AnnotationDocument.class)
                .setParameter("project", aSourceDocument.getProject())
                .setParameter("document", aSourceDocument).getResultList();
    }

    @Override
    public List<String> listGuidelines(Project aProject)
    {
        // list all guideline files
        File[] files = new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + GUIDELINE)
                .listFiles();

        // Name of the guideline files
        List<String> annotationGuidelineFiles = new ArrayList<String>();
        if (files != null) {
            for (File file : files) {
                annotationGuidelineFiles.add(file.getName());
            }
        }

        return annotationGuidelineFiles;
    }

    @Override
    public List<String> listTemplates(Project aProject)
    {
        // list all MIRA template files
        File[] files = new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + MIRA
                + MIRA_TEMPLATE).listFiles();

        // Name of the MIRA template files
        List<String> templateFiles = new ArrayList<String>();
        if (files != null) {
            for (File file : files) {
                templateFiles.add(file.getName());
            }
        }

        return templateFiles;
    }

    @Override
    @Transactional
    public List<Project> listProjects()
    {
        return entityManager.createQuery("FROM Project  ORDER BY name ASC ", Project.class)
                .getResultList();
    }

    @Override
    @Transactional
    public List<CrowdJob> listCrowdJobs()
    {
        return entityManager.createQuery("FROM CrowdJob", CrowdJob.class).getResultList();
    }

    @Override
    @Transactional
    public List<CrowdJob> listCrowdJobs(Project aProject)
    {
        return entityManager.createQuery("FROM CrowdJob where project =:project", CrowdJob.class)
                .setParameter("project", aProject).getResultList();
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<SourceDocument> listSourceDocuments(Project aProject)
    {
        List<SourceDocument> sourceDocuments = entityManager
                .createQuery("FROM SourceDocument where project =:project", SourceDocument.class)
                .setParameter("project", aProject).getResultList();
        List<SourceDocument> tabSepDocuments = new ArrayList<SourceDocument>();
        for (SourceDocument sourceDocument : sourceDocuments) {
            if (sourceDocument.getFormat().equals(WebAnnoConst.TAB_SEP)) {
                tabSepDocuments.add(sourceDocument);
            }
        }
        sourceDocuments.removeAll(tabSepDocuments);
        return sourceDocuments;
    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public List<SourceDocument> listTabSepDocuments(Project aProject)
    {
        List<SourceDocument> sourceDocuments = entityManager
                .createQuery("FROM SourceDocument where project =:project", SourceDocument.class)
                .setParameter("project", aProject).getResultList();
        List<SourceDocument> tabSepDocuments = new ArrayList<SourceDocument>();
        for (SourceDocument sourceDocument : sourceDocuments) {
            if (sourceDocument.getFormat().equals(WebAnnoConst.TAB_SEP)) {
                tabSepDocuments.add(sourceDocument);
            }
        }
        return tabSepDocuments;
    }

    @Override
    @Transactional
    public List<User> listUsers()
    {
        return entityManager.createQuery("FROM User", User.class).getResultList();
    }

    @Override
    public Properties loadUserSettings(String aUsername, Project aProject)
        throws FileNotFoundException, IOException
    {
        Properties property = new Properties();
        property.load(new FileInputStream(new File(dir.getAbsolutePath() + PROJECT
                + aProject.getId() + SETTINGS + aUsername + "/"
                + annotationPreferencePropertiesFileName)));
        return property;
    }

    @Override
    public Properties loadHelpContents()
        throws FileNotFoundException, IOException
    {
        if (new File(dir.getAbsolutePath() + HELP_FILE).exists()) {
            Properties property = new Properties();
            property.load(new FileInputStream(new File(dir.getAbsolutePath() + HELP_FILE)));
            return property;
        }
        else {
            return helpProperiesFile;
        }

    }

    @Override
    @Transactional
    public void removeProject(Project aProject, User aUser)
        throws IOException
    {

        // remove, if exists, a crowdsource job created from this project
        for (CrowdJob crowdJob : listCrowdJobs(aProject)) {
            removeCrowdJob(crowdJob);
        }
        for (SourceDocument document : listSourceDocuments(aProject)) {
            removeSourceDocument(document);
        }

        for (SourceDocument document : listTabSepDocuments(aProject)) {
            removeSourceDocument(document);
        }

        for (MiraTemplate template : listMiraTemplates(aProject)) {
            removeMiraTemplate(template);
        }

        for (AnnotationFeature feature : annotationService.listAnnotationFeature(aProject)) {
            annotationService.removeAnnotationFeature(feature);
        }

        // remove the layers too
        for (AnnotationLayer layer : annotationService.listAnnotationLayer(aProject)) {
            annotationService.removeAnnotationLayer(layer);
        }

        for (TagSet tagSet : annotationService.listTagSets(aProject)) {
            annotationService.removeTagSet(tagSet);
        }

        // remove the project directory from the file system
        String path = dir.getAbsolutePath() + PROJECT + aProject.getId();
        try {
            FileUtils.deleteDirectory(new File(path));
        }
        catch (FileNotFoundException e) {
            createLog(aProject, aUser.getUsername()).warn(
                    "Project directory to be deleted was not found: [" + path + "]. Ignoring.");
        }

        for (ProjectPermission permisions : getProjectPermisions(aProject)) {
            entityManager.remove(permisions);
        }
        // remove metadata from DB
        entityManager.remove(aProject);
        createLog(aProject, aUser.getUsername()).info(
                " Removed Project [" + aProject.getName() + "] with ID [" + aProject.getId() + "]");
        createLog(aProject, aUser.getUsername()).removeAllAppenders();

    }

    @Override
    @Transactional
    public void removeCrowdJob(CrowdJob crowdProject)
    {
        entityManager.remove(entityManager.merge(crowdProject));
    }

    @Override
    public void removeGuideline(Project aProject, String aFileName, String username)
        throws IOException
    {
        FileUtils.forceDelete(new File(dir.getAbsolutePath() + PROJECT + aProject.getId()
                + GUIDELINE + aFileName));
        createLog(aProject, username).info(
                " Removed Guideline file from [" + aProject.getName() + "] with ID ["
                        + aProject.getId() + "]");
        createLog(aProject, username).removeAllAppenders();
    }

    @Override
    public void removeTemplate(Project aProject, String aFileName, String username)
        throws IOException
    {
        FileUtils.forceDelete(new File(dir.getAbsolutePath() + PROJECT + aProject.getId() + MIRA
                + MIRA_TEMPLATE + aFileName));
        createLog(aProject, username).info(
                " Removed Template file from [" + aProject.getName() + "] with ID ["
                        + aProject.getId() + "]");
        createLog(aProject, username).removeAllAppenders();
    }

    @Override
    public void removeCurationDocumentContent(SourceDocument aSourceDocument, String aUsername)
        throws IOException
    {
        if (new File(getAnnotationFolder(aSourceDocument), WebAnnoConst.CURATION_USER + ".ser")
                .exists()) {
            FileUtils.forceDelete(new File(getAnnotationFolder(aSourceDocument),
                    WebAnnoConst.CURATION_USER + ".ser"));

            createLog(aSourceDocument.getProject(), aUsername).info(
                    " Removed Curated document from  project [" + aSourceDocument.getProject()
                            + "] for the source document [" + aSourceDocument.getId());
            createLog(aSourceDocument.getProject(), aUsername).removeAllAppenders();
        }
    }

    @Override
    @Transactional
    public void removeProjectPermission(ProjectPermission projectPermission)
        throws IOException
    {
        entityManager.remove(projectPermission);
        createLog(projectPermission.getProject(), projectPermission.getUser()).info(
                " Removed Project Permission [" + projectPermission.getLevel() + "] for the USer ["
                        + projectPermission.getUser() + "] From project ["
                        + projectPermission.getProject().getId() + "]");
        createLog(projectPermission.getProject(), projectPermission.getUser()).removeAllAppenders();

    }

    @Override
    @Transactional
    public void removeSourceDocument(SourceDocument aDocument)
        throws IOException
    {

        for (AnnotationDocument annotationDocument : listAllAnnotationDocuments(aDocument)) {
            removeAnnotationDocument(annotationDocument);
        }
        // remove it from the crowd job, if it belongs already
        for (CrowdJob crowdJob : listCrowdJobs(aDocument.getProject())) {
            if (crowdJob.getDocuments().contains(aDocument)) {
                crowdJob.getDocuments().remove(aDocument);
                entityManager.persist(crowdJob);
            }
        }

        entityManager.remove(aDocument);

        String path = dir.getAbsolutePath() + PROJECT + aDocument.getProject().getId() + DOCUMENT
                + aDocument.getId();
        // remove from file both source and related annotation file
        if (new File(path).exists()) {
            FileUtils.forceDelete(new File(path));
        }
        
        createLog(aDocument.getProject(), getLogUser()).info(
                " Removed Document [" + aDocument.getName() + "] with ID [" + aDocument.getId()
                        + "] from Project [" + aDocument.getProject().getId() + "]");
        createLog(aDocument.getProject(), getLogUser()).removeAllAppenders();

    }

    @Override
    @Transactional
    public void removeAnnotationDocument(AnnotationDocument aAnnotationDocument)
    {
        entityManager.remove(aAnnotationDocument);
    }

    public void setDir(File aDir)
    {
        dir = aDir;
    }

    @Override
    public void savePropertiesFile(Project aProject, InputStream aIs, String aFileName)
        throws IOException
    {
        String path = dir.getAbsolutePath() + PROJECT + aProject.getId() + "/"
                + FilenameUtils.getFullPath(aFileName);
        FileUtils.forceMkdir(new File(path));

        File newTcfFile = new File(path, FilenameUtils.getName(aFileName));
        OutputStream os = null;
        try {
            os = new FileOutputStream(newTcfFile);
            copyLarge(aIs, os);
        }
        finally {
            closeQuietly(os);
            closeQuietly(aIs);
        }

    }

    @Override
    public <T> void saveUserSettings(String aUsername, Project aProject, Mode aSubject,
            T aConfigurationObject)
        throws IOException
    {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(aConfigurationObject);
        Properties property = new Properties();
        for (PropertyDescriptor value : wrapper.getPropertyDescriptors()) {
            if (wrapper.getPropertyValue(value.getName()) == null) {
                continue;
            }
            property.setProperty(aSubject + "." + value.getName(),
                    wrapper.getPropertyValue(value.getName()).toString());
        }
        String propertiesPath = dir.getAbsolutePath() + PROJECT + aProject.getId() + SETTINGS
                + aUsername;
        // append existing preferences for the other mode
        if (new File(propertiesPath, annotationPreferencePropertiesFileName).exists()) {
            // aSubject = aSubject.equals(Mode.ANNOTATION) ? Mode.CURATION :
            // Mode.ANNOTATION;
            for (Entry<Object, Object> entry : loadUserSettings(aUsername, aProject).entrySet()) {
                String key = entry.getKey().toString();
                // Maintain other Modes of annotations confs than this one
                if (!key.substring(0, key.indexOf(".")).equals(aSubject.toString())) {
                    property.put(entry.getKey(), entry.getValue());
                }
            }
        }
        FileUtils
                .forceDeleteOnExit(new File(propertiesPath, annotationPreferencePropertiesFileName));
        FileUtils.forceMkdir(new File(propertiesPath));
        property.store(new FileOutputStream(new File(propertiesPath,
                annotationPreferencePropertiesFileName)), null);

        createLog(aProject, aUsername).info(
                " Saved preferences file [" + annotationPreferencePropertiesFileName
                        + "] for project [" + aProject.getName() + "] with ID [" + aProject.getId()
                        + "] to location: [" + propertiesPath + "]");
        createLog(aProject, aUsername).removeAllAppenders();

    }

    @Override
    public <T> void saveHelpContents(T aConfigurationObject)
        throws IOException
    {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(aConfigurationObject);
        Properties property = new Properties();
        for (PropertyDescriptor value : wrapper.getPropertyDescriptors()) {
            if (wrapper.getPropertyValue(value.getName()) == null) {
                continue;
            }
            property.setProperty(value.getName(), wrapper.getPropertyValue(value.getName())
                    .toString());
        }
        File helpFile = new File(dir.getAbsolutePath() + HELP_FILE);
        if (helpFile.exists()) {
            FileUtils.forceDeleteOnExit(helpFile);
        }
        else {
            helpFile.createNewFile();
        }
        property.store(new FileOutputStream(helpFile), null);

    }

    @Override
    @Transactional
    public void uploadSourceDocument(File aFile, SourceDocument aDocument)
        throws IOException
    {
        // Check if the file has a valid format / can be converted without error
        JCas cas = null;
        try {
            if (aDocument.getFormat().equals(WebAnnoConst.TAB_SEP)) {
                if (!isTabSepFileFormatCorrect(aFile)) {
                    throw new IOException(
                            "This TAB-SEP file is not in correct format. It should have two columns separated by TAB!");
                }
            }
            else {
                cas = convertSourceDocumentToCas(aFile,
                        getReadableFormats().get(aDocument.getFormat()), aDocument);
            }
        }
        catch (IOException e) {
            removeSourceDocument(aDocument);
            throw e;
        }
        catch (Exception e) {
            removeSourceDocument(aDocument);
            throw new IOException(e.getMessage(), e);
        }

        // Copy the original file into the repository
        File targetFile = getSourceDocumentFile(aDocument);
        FileUtils.forceMkdir(targetFile.getParentFile());
        FileUtils.copyFile(aFile, targetFile);
        
        // Copy the initial conversion of the file into the repository
        if (cas != null) {
            writeSerializedCas(cas, getCasFile(aDocument, INITIAL_CAS_PSEUDO_USER));
        }
        
        createLog(aDocument.getProject(), getLogUser()).info(
                " Imported file [" + aDocument.getName() + "] with ID [" + aDocument.getId()
                        + "] to Project [" + aDocument.getProject().getId() + "]");
        createLog(aDocument.getProject(), getLogUser()).removeAllAppenders();
    }

    @Override
    @Transactional
    @Deprecated
    public void uploadSourceDocument(InputStream aIs, SourceDocument aDocument)
        throws IOException
    {
        File targetFile = getSourceDocumentFile(aDocument);
        FileUtils.forceMkdir(targetFile.getParentFile());
        
        OutputStream os = null;
        try {
            os = new FileOutputStream(targetFile);
            copyLarge(aIs, os);
        }
        finally {
            closeQuietly(os);
            closeQuietly(aIs);
        }

        createLog(aDocument.getProject(), getLogUser()).info(
                " Imported file [" + aDocument.getName() + "] with ID [" + aDocument.getId()
                        + "] to Project [" + aDocument.getProject().getId() + "]");
        createLog(aDocument.getProject(), getLogUser()).removeAllAppenders();

    }

    @Override
    public List<String> getReadableFormatLabels()
        throws ClassNotFoundException
    {
        List<String> readableFormats = new ArrayList<String>();
        for (String key : readWriteFileFormats.stringPropertyNames()) {
            if (key.contains(".label") && !isBlank(readWriteFileFormats.getProperty(key))) {
                String readerLabel = key.substring(0, key.lastIndexOf(".label"));
                if (!isBlank(readWriteFileFormats.getProperty(readerLabel + ".reader"))) {
                    readableFormats.add(readWriteFileFormats.getProperty(key));
                }
            }
        }
        Collections.sort(readableFormats);
        return readableFormats;
    }

    @Override
    public String getReadableFormatId(String aLabel)
        throws ClassNotFoundException
    {
        String readableFormat = "";
        for (String key : readWriteFileFormats.stringPropertyNames()) {
            if (key.contains(".label") && !isBlank(readWriteFileFormats.getProperty(key))) {
                if (readWriteFileFormats.getProperty(key).equals(aLabel)) {
                    readableFormat = key.substring(0, key.lastIndexOf(".label"));
                    break;
                }
            }
        }
        return readableFormat;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, Class> getReadableFormats()
        throws ClassNotFoundException
    {
        Map<String, Class> readableFormats = new HashMap<String, Class>();
        for (String key : readWriteFileFormats.stringPropertyNames()) {
            if (key.contains(".label") && !isBlank(readWriteFileFormats.getProperty(key))) {
                String readerLabel = key.substring(0, key.lastIndexOf(".label"));
                if (!isBlank(readWriteFileFormats.getProperty(readerLabel + ".reader"))) {
                    readableFormats.put(readerLabel, Class.forName(readWriteFileFormats
                            .getProperty(readerLabel + ".reader")));
                }
            }
        }
        return readableFormats;
    }

    @Override
    public List<String> getWritableFormatLabels()
        throws ClassNotFoundException
    {
        List<String> writableFormats = new ArrayList<String>();
        for (String key : readWriteFileFormats.stringPropertyNames()) {
            if (key.contains(".label") && !isBlank(readWriteFileFormats.getProperty(key))) {
                String writerLabel = key.substring(0, key.lastIndexOf(".label"));
                if (!isBlank(readWriteFileFormats.getProperty(writerLabel + ".writer"))) {
                    writableFormats.add(readWriteFileFormats.getProperty(key));
                }
            }
        }
        Collections.sort(writableFormats);
        return writableFormats;
    }

    @Override
    public String getWritableFormatId(String aLabel)
        throws ClassNotFoundException
    {
        String writableFormat = "";
        for (String key : readWriteFileFormats.stringPropertyNames()) {
            if (key.contains(".label") && !isBlank(readWriteFileFormats.getProperty(key))) {
                if (readWriteFileFormats.getProperty(key).equals(aLabel)) {
                    writableFormat = key.substring(0, key.lastIndexOf(".label"));
                    break;
                }
            }
        }
        return writableFormat;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<String, Class> getWritableFormats()
        throws ClassNotFoundException
    {
        Map<String, Class> writableFormats = new HashMap<String, Class>();
        Set<String> keys = (Set) readWriteFileFormats.keySet();

        for (String keyvalue : keys) {
            if (keyvalue.contains(".label")) {
                String writerLabel = keyvalue.substring(0, keyvalue.lastIndexOf(".label"));
                if (readWriteFileFormats.getProperty(writerLabel + ".writer") != null) {
                    writableFormats.put(writerLabel, Class.forName(readWriteFileFormats
                            .getProperty(writerLabel + ".writer")));
                }
            }
        }
        return writableFormats;
    }

    public String getAnnotationPreferencePropertiesFileName()
    {
        return annotationPreferencePropertiesFileName;
    }

    public void setAnnotationPreferencePropertiesFileName(
            String aAnnotationPreferencePropertiesFileName)
    {
        annotationPreferencePropertiesFileName = aAnnotationPreferencePropertiesFileName;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public void writeCorrectionCas(JCas aJcas, SourceDocument aDocument, User aUser)
        throws IOException
    {
        writeCas(aDocument, aJcas, CORRECTION_USER, aUser);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public void writeCurationCas(JCas aJcas, SourceDocument aDocument, User aUser)
        throws IOException
    {
        writeCas(aDocument, aJcas, CURATION_USER, aUser);
    }

    @Override
    public JCas readCorrectionCas(SourceDocument aDocument)
        throws UIMAException, IOException, ClassNotFoundException
    {
        return readCas(aDocument, CORRECTION_USER);
    }

    @Override
    public JCas readCurationCas(SourceDocument aDocument)
        throws UIMAException, IOException, ClassNotFoundException
    {
        return readCas(aDocument, CURATION_USER);
    }

    /**
     * Creates an annotation document (either user's annotation document or CURATION_USER's
     * annotation document)
     *
     * @param aDocument
     *            the {@link SourceDocument}
     * @param aJcas
     *            The annotated CAS object
     * @param aUserName
     *            the user who annotates the document if it is user's annotation document OR the
     *            CURATION_USER
     * @param aUser
     *            The user who annotates the document OR the curator who curates the document
     * @throws IOException
     */

    private void writeCas(SourceDocument aDocument, JCas aJcas, String aUserName, User aUser)
        throws IOException
    {
        log.debug("Updating annotation document [" + aDocument.getName() + "] " + "with ID ["
                + aDocument.getId() + "] in project ID [" + aDocument.getProject().getId()
                + "] for user [" + aUser.getUsername() + "]");
        //DebugUtils.smallStack();
       
        synchronized (lock) {
            File annotationFolder = getAnnotationFolder(aDocument);
            FileUtils.forceMkdir(annotationFolder);

            final String username = aUserName;

            File currentVersion = new File(annotationFolder, username + ".ser");
            File oldVersion = new File(annotationFolder, username + ".ser.old");

            // Save current version
            try {
                // Make a backup of the current version of the file before overwriting
                if (currentVersion.exists()) {
                    renameFile(currentVersion, oldVersion);
                }

                // Now write the new version to "<username>.ser" or CURATION_USER.ser
                DocumentMetaData md;
                try {
                    md = DocumentMetaData.get(aJcas);
                }
                catch (IllegalArgumentException e) {
                    md = DocumentMetaData.create(aJcas);
                }
                md.setDocumentId(aUserName);
                
                File targetPath = getAnnotationFolder(aDocument);
                writeSerializedCas(aJcas, new File(targetPath, aUserName+".ser"));
                
                createLog(aDocument.getProject(), aUser.getUsername()).info(
                        "Updated annotation document [" + aDocument.getName() + "] " + "with ID ["
                                + aDocument.getId() + "] in project ID ["
                                + aDocument.getProject().getId() + "] for user ["
                                + aUser.getUsername() + "]");
                createLog(aDocument.getProject(), aUser.getUsername()).removeAllAppenders();

                // If the saving was successful, we delete the old version
                if (oldVersion.exists()) {
                    FileUtils.forceDelete(oldVersion);
                }
            }
            catch (IOException e) {
                // If we could not save the new version, restore the old one.
                FileUtils.forceDelete(currentVersion);
                // If this is the first version, there is no old version, so do not restore anything
                if (oldVersion.exists()) {
                    renameFile(oldVersion, currentVersion);
                }
                // Now abort anyway
                throw e;
            }

            // Manage history
            if (backupInterval > 0) {
                // Determine the reference point in time based on the current version
                long now = currentVersion.lastModified();

                // Get all history files for the current user
                File[] history = annotationFolder.listFiles(new FileFilter()
                {
                    private final Matcher matcher = Pattern.compile(
                            Pattern.quote(username) + "\\.ser\\.[0-9]+\\.bak").matcher("");

                    @Override
                    public boolean accept(File aFile)
                    {
                        // Check if the filename matches the pattern given above.
                        return matcher.reset(aFile.getName()).matches();
                    }
                });

                // Sort the files (oldest one first)
                Arrays.sort(history, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);

                // Check if we need to make a new history file
                boolean historyFileCreated = false;
                File historyFile = new File(annotationFolder, username + ".ser." + now + ".bak");
                if (history.length == 0) {
                    // If there is no history yet but we should keep history, then we create a
                    // history file in any case.
                    FileUtils.copyFile(currentVersion, historyFile);
                    historyFileCreated = true;
                }
                else {
                    // Check if the newest history file is significantly older than the current one
                    File latestHistory = history[history.length - 1];
                    if (latestHistory.lastModified() + backupInterval < now) {
                        FileUtils.copyFile(currentVersion, historyFile);
                        historyFileCreated = true;
                    }
                }

                // Prune history based on number of backup
                if (historyFileCreated) {
                    // The new version is not in the history, so we keep that in any case. That
                    // means we need to keep one less.
                    int toKeep = Math.max(backupKeepNumber - 1, 0);
                    if ((backupKeepNumber > 0) && (toKeep < history.length)) {
                        // Copy the oldest files to a new array
                        File[] toRemove = new File[history.length - toKeep];
                        System.arraycopy(history, 0, toRemove, 0, toRemove.length);

                        // Restrict the history to what is left
                        File[] newHistory = new File[toKeep];
                        if (toKeep > 0) {
                            System.arraycopy(history, toRemove.length, newHistory, 0,
                                    newHistory.length);
                        }
                        history = newHistory;

                        // Remove these old files
                        for (File file : toRemove) {
                            FileUtils.forceDelete(file);
                            createLog(aDocument.getProject(), aUser.getUsername()).info(
                                    "Removed surplus history file [" + file.getName() + "] "
                                            + "for document with ID [" + aDocument.getId()
                                            + "] in project ID [" + aDocument.getProject().getId()
                                            + "]");
                            createLog(aDocument.getProject(), aUser.getUsername())
                                    .removeAllAppenders();
                        }
                    }

                    // Prune history based on time
                    if (backupKeepTime > 0) {
                        for (File file : history) {
                            if ((file.lastModified() + backupKeepTime) < now) {
                                FileUtils.forceDelete(file);
                                createLog(aDocument.getProject(), aUser.getUsername()).info(
                                        "Removed outdated history file [" + file.getName() + "] "
                                                + " for document with ID [" + aDocument.getId()
                                                + "] in project ID ["
                                                + aDocument.getProject().getId() + "]");
                                createLog(aDocument.getProject(), aUser.getUsername())
                                        .removeAllAppenders();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * For a given {@link SourceDocument}, return the {@link AnnotationDocument} for the user or for
     * the CURATION_USER
     *
     * @param aDocument
     *            the {@link SourceDocument}
     * @param aUsername
     *            the {@link User} who annotates the {@link SourceDocument} or the CURATION_USER
     */
    private JCas readCas(SourceDocument aDocument, String aUsername)
        throws IOException
    {
        if (log.isDebugEnabled()) {
            log.debug("Getting annotation document [" + aDocument.getName() + "] with ID ["
                    + aDocument.getId() + "] in project ID [" + aDocument.getProject().getId()
                    + "] for user [" + aUsername + "]");
        }
        
        //DebugUtils.smallStack();
        
        synchronized (lock) {
            File annotationFolder = getAnnotationFolder(aDocument);

            String file = aUsername + ".ser";

            try {
                File serializedCasFile = new File(annotationFolder, file);
                if (!serializedCasFile.exists()) {
                    throw new FileNotFoundException("Annotation document of user [" + aUsername
                            + "] for source document [" + aDocument.getName() + "] ("
                            + aDocument.getId() + "). not found in project["
                            + aDocument.getProject().getName() + "] ("
                            + aDocument.getProject().getId() + ")");
                }
                
                CAS cas = CasCreationUtils.createCas((TypeSystemDescription) null, null, null);
                readSerializedCas(cas.getJCas(), serializedCasFile);
                return cas.getJCas();
            }
            catch (UIMAException e) {
                throw new DataRetrievalFailureException("Unable to parse annotation", e);
            }
        }
    }

    @Override
    public boolean isRemoteProject(Project project)
    {
        return new File(dir, PROJECT + project.getId() + META_INF).exists();
    }

    private List<String> getAllAnnotators(Project aProject)
    {
        // Get all annotators in the project
        List<String> users = entityManager
                .createQuery(
                        "SELECT DISTINCT user FROM ProjectPermission WHERE project = :project "
                                + "AND level = :level", String.class)
                .setParameter("project", aProject).setParameter("level", PermissionLevel.USER)
                .getResultList();

        // check if the username is in the Users database (imported projects
        // might have username
        // in the ProjectPermission entry while it is not in the Users database
        List<String> notInUsers = new ArrayList<String>();
        for (String user : users) {
            if (!userRepository.exists(user)) {
                notInUsers.add(user);
            }
        }
        users.removeAll(notInUsers);

        return users;
    }

    @Override
    @Deprecated
    public void upgradeCasAndSave(SourceDocument aDocument, Mode aMode, String aUsername)
        throws IOException
    {
        User user = getUser(aUsername);
        if (existsAnnotationDocument(aDocument, user)) {
            log.debug("Upgrading annotation document [" + aDocument.getName() + "] " + "with ID ["
                    + aDocument.getId() + "] in project ID [" + aDocument.getProject().getId()
                    + "] for user [" + aUsername + "] in mode [" + aMode + "]");
            //DebugUtils.smallStack();
            
            AnnotationDocument annotationDocument = getAnnotationDocument(aDocument, user);
            try {
                CAS cas = readAnnotationCas(annotationDocument).getCas();
                upgradeCas(cas, annotationDocument, aMode);
                writeAnnotationCas(cas.getJCas(),
                        annotationDocument.getDocument(), user);
                
                if (aMode.equals(Mode.ANNOTATION)) {
                    // In this case we only need to upgrade to annotation document
                }
                else if (aMode.equals(Mode.AUTOMATION) || aMode.equals(Mode.CORRECTION)) {
                    CAS corrCas = readCorrectionCas(aDocument).getCas();
                    upgradeCas(corrCas, annotationDocument, aMode);
                    writeCorrectionCas(corrCas.getJCas(), aDocument, user);
                }
                else {
                    CAS curCas = readCurationCas(aDocument).getCas();
                    upgradeCas(curCas, annotationDocument, aMode);
                    writeCurationCas(curCas.getJCas(), aDocument, user);
                }

            }
            catch (Exception e) {
                // no need to catch, it is acceptable that no curation document
                // exists to be upgraded while there are annotation documents
            }
            createLog(aDocument.getProject(), aUsername).info(
                    "Upgraded annotation document [" + aDocument.getName() + "] " + "with ID ["
                            + aDocument.getId() + "] in project ID ["
                            + aDocument.getProject().getId() + "] for user [" + aUsername
                            + "] in mode [" + aMode + "]");
            createLog(aDocument.getProject(), aUsername).removeAllAppenders();
        }
    }

    @Override
    public void upgradeCas(CAS aCas, AnnotationDocument aAnnotationDocument, Mode aMode)
        throws UIMAException, IOException
    {
        upgradeCas(aCas, aAnnotationDocument.getDocument(), aAnnotationDocument.getUser(), aMode);
    }
    
    private void upgradeCas(CAS aCas, SourceDocument aSourceDocument, String aUser, Mode aMode)
        throws UIMAException, IOException
    {
        TypeSystemDescription builtInTypes = TypeSystemDescriptionFactory
                .createTypeSystemDescription();
        List<TypeSystemDescription> projectTypes = getProjectTypes(aSourceDocument.getProject());
        projectTypes.add(builtInTypes);
        TypeSystemDescription allTypes = CasCreationUtils.mergeTypeSystems(projectTypes);

        // Prepare template for new CAS
        CAS newCas = JCasFactory.createJCas(allTypes).getCas();
        CASCompleteSerializer serializer = Serialization.serializeCASComplete((CASImpl) newCas);

        // Save old type system
        TypeSystem oldTypeSystem = aCas.getTypeSystem();

        // Save old CAS contents
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        Serialization.serializeWithCompression(aCas, os2, oldTypeSystem);

        // Prepare CAS with new type system
        Serialization.deserializeCASComplete(serializer, (CASImpl) aCas);

        // Restore CAS data to new type system
        Serialization.deserializeCAS(aCas, new ByteArrayInputStream(os2.toByteArray()),
                oldTypeSystem, null);

        createLog(aSourceDocument.getProject(), aUser).info(
                "Upgraded CAS of user [" + aUser + "] for document [" + aSourceDocument.getName()
                        + "] " + " in project ID [" + aSourceDocument.getProject().getId()
                        + "] in mode [" + aMode + "]");
        createLog(aSourceDocument.getProject(), aUser).removeAllAppenders();
    }

    @Override
    @Transactional
    public JCas readAnnotationCas(SourceDocument aDocument, User aUser)
        throws UIMAException, IOException, ClassNotFoundException
    {
        // Change the state of the source document to in progress
        aDocument.setState(SourceDocumentStateTransition
                .transition(SourceDocumentStateTransition.NEW_TO_ANNOTATION_IN_PROGRESS));
        
        // Make sure we have an AnnotationDocument in the database
        AnnotationDocument annotationDocument = null;
        if (!existsAnnotationDocument(aDocument, aUser)) {
            annotationDocument = new AnnotationDocument();
            annotationDocument.setDocument(aDocument);
            annotationDocument.setName(aDocument.getName());
            annotationDocument.setUser(aUser.getUsername());
            annotationDocument.setProject(aDocument.getProject());
            createAnnotationDocument(annotationDocument);
        }
        else {
            annotationDocument = getAnnotationDocument(aDocument, aUser);
        }
        
        // Make sure we have a CAS
        JCas jcas;
        if (!existsCas(aDocument, aUser.getUsername())) {
            // Initial conversion
            try {
                jcas = convertSourceDocumentToCas(getSourceDocumentFile(aDocument),
                        getReadableFormats().get(aDocument.getFormat()), aDocument);
            }
            catch (UIMAException e) {
                throw new IOException(e);
            }
            catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
            catch (Exception e) {
                throw new IOException(e.getMessage() != null ? e.getMessage()
                        : "This is an invalid file. The reader for the document "
                                + aDocument.getName() + " can't read this " + aDocument.getFormat()
                                + " file type");
            }
            writeAnnotationCas(jcas, aDocument, aUser);
        }
        else {
            // Read existing
            jcas =  readCas(annotationDocument.getDocument(), annotationDocument.getUser());
        }
        
        return jcas;
    }

    @Override
    @Transactional
    public void writeCas(Mode aMode, SourceDocument aSourceDocument, User aUser, JCas aJcas)
        throws IOException
    {
        if (aMode.equals(Mode.ANNOTATION) || aMode.equals(Mode.AUTOMATION)
                || aMode.equals(Mode.CORRECTION) || aMode.equals(Mode.CORRECTION_MERGE)) {
            writeAnnotationCas(aJcas, aSourceDocument, aUser);
        }
        else if (aMode.equals(Mode.CURATION) || aMode.equals(Mode.CURATION_MERGE)) {
            writeCurationCas(aJcas, aSourceDocument, aUser);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JCas convertSourceDocumentToCas(File aFile, Class aReader, SourceDocument aDocument)
        throws UIMAException, IOException
    {
        // Prepare a CAS with the project type system
        TypeSystemDescription builtInTypes = TypeSystemDescriptionFactory
                .createTypeSystemDescription();
        List<TypeSystemDescription> projectTypes = getProjectTypes(aDocument.getProject());
        projectTypes.add(builtInTypes);
        TypeSystemDescription allTypes = CasCreationUtils.mergeTypeSystems(projectTypes);
        CAS cas = JCasFactory.createJCas(allTypes).getCas();

        // Convert the source document to CAS
        CollectionReader reader = CollectionReaderFactory.createReader(aReader,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, aFile.getParentFile().getAbsolutePath(), 
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]" + aFile.getName() });
        if (!reader.hasNext()) {
            throw new FileNotFoundException("Annotation file [" + aFile.getName()
                    + "] not found in [" + aFile.getPath() + "]");
        }
        reader.getNext(cas);
        JCas jCas = cas.getJCas();
        
        // Create sentence / token annotations if they are missing
        boolean hasTokens = JCasUtil.exists(jCas, Token.class);
        boolean hasSentences = JCasUtil.exists(jCas, Sentence.class);
        
        if (!hasTokens || !hasSentences) {
            AnalysisEngine pipeline = createEngine(createEngineDescription(
                    BreakIteratorSegmenter.class, 
                    BreakIteratorSegmenter.PARAM_WRITE_TOKEN, !hasTokens, 
                    BreakIteratorSegmenter.PARAM_WRITE_SENTENCE, !hasSentences));
            pipeline.process(cas.getJCas());
        }
        
        return jCas;
    }

    @Override
    @Transactional
    public void updateTimeStamp(SourceDocument aDocument, User aUser, Mode aMode)
        throws IOException
    {
        if (aMode.equals(Mode.CURATION)) {
            aDocument.setTimestamp(new Timestamp(new Date().getTime()));
            entityManager.merge(aDocument);
        }
        else {
            AnnotationDocument annotationDocument = getAnnotationDocument(aDocument, aUser);
            annotationDocument.setSentenceAccessed(aDocument.getSentenceAccessed());
            annotationDocument.setTimestamp(new Timestamp(new Date().getTime()));
            annotationDocument.setState(AnnotationDocumentState.IN_PROGRESS);
            entityManager.merge(annotationDocument);
        }
    }

    @Override
    public String getDatabaseDriverName()
    {
        final StringBuilder sb = new StringBuilder();
        Session session = entityManager.unwrap(Session.class);
        session.doWork(new Work()
        {
            @Override
            public void execute(Connection aConnection)
                throws SQLException
            {
                sb.append(aConnection.getMetaData().getDriverName());
            }
        });

        return sb.toString();
    }

    @Override
    public int isCrowdSourceEnabled()
    {
        return crowdsourceEnabled;
    }

    @Override
    public File getMiraModel(AnnotationFeature aFeature, boolean aOtherLayer,
            SourceDocument aDocument)
    {
        if (aDocument != null) {
            return new File(getMiraDir(aFeature), aDocument.getId() + "- "
                    + aDocument.getProject().getId() + "-model");
        }
        else if (aOtherLayer) {
            return new File(getMiraDir(aFeature), aFeature.getId() + "-model");
        }
        else {
            return new File(getMiraDir(aFeature), aFeature.getLayer().getId() + "-"
                    + aFeature.getId() + "-model");
        }
    }

    @Override
    public File getMiraDir(AnnotationFeature aFeature)
    {
        return new File(dir, PROJECT + aFeature.getProject().getId() + MIRA);
    }

    @Override
    @Transactional
    public void createTemplate(MiraTemplate aTemplate)
    {
        if (aTemplate.getId() == 0) {
            entityManager.persist(aTemplate);
        }
        else {
            entityManager.merge(aTemplate);
        }

    }

    @Override
    @Transactional(noRollbackFor = NoResultException.class)
    public MiraTemplate getMiraTemplate(AnnotationFeature aFeature)
    {

        return entityManager
                .createQuery("FROM MiraTemplate WHERE trainFeature =:trainFeature",
                        MiraTemplate.class).setParameter("trainFeature", aFeature)
                .getSingleResult();
    }

    @Override
    public boolean existsMiraTemplate(AnnotationFeature aFeature)
    {
        try {
            entityManager
                    .createQuery("FROM MiraTemplate WHERE trainFeature =:trainFeature",
                            MiraTemplate.class).setParameter("trainFeature", aFeature)
                    .getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;

        }
    }

    @Override
    public List<MiraTemplate> listMiraTemplates(Project aProject)
    {
        List<MiraTemplate> allTenplates = entityManager.createQuery(
                "FROM MiraTemplate ORDER BY trainFeature ASC ", MiraTemplate.class).getResultList();
        List<MiraTemplate> templatesInThisProject = new ArrayList<MiraTemplate>();
        for (MiraTemplate miraTemplate : allTenplates) {
            if (miraTemplate.getTrainFeature() != null
                    && miraTemplate.getTrainFeature().getProject().getId() == aProject.getId()) {
                templatesInThisProject.add(miraTemplate);
            }
        }
        return templatesInThisProject;
    }

    @Override
    @Transactional
    public void removeMiraTemplate(MiraTemplate aTemplate)
    {
        try {
            removeAutomationStatus(getAutomationStatus(aTemplate));
        }
        catch (NoResultException e) {
            // do nothing - automation was not started and no status created for this template
        }
        entityManager.remove(aTemplate);
    }

    @Override
    @Transactional
    public void removeAutomationStatus(AutomationStatus aStstus)
    {

        entityManager.remove(aStstus);
    }

    private List<TypeSystemDescription> getProjectTypes(Project aProject)
    {
        // Create a new type system from scratch
        List<TypeSystemDescription> types = new ArrayList<TypeSystemDescription>();
        for (AnnotationLayer type : annotationService.listAnnotationLayer(aProject)) {
            if (type.getType().equals(SPAN_TYPE) && !type.isBuiltIn()) {
                TypeSystemDescription tsd = new TypeSystemDescription_impl();
                TypeDescription td = tsd.addType(type.getName(), "", CAS.TYPE_NAME_ANNOTATION);
                List<AnnotationFeature> features = annotationService.listAnnotationFeature(type);
                for (AnnotationFeature feature : features) {
                    generateFeature(tsd, td, feature);
                }

                types.add(tsd);
            }
            else if (type.getType().equals(RELATION_TYPE) && !type.isBuiltIn()) {
                TypeSystemDescription tsd = new TypeSystemDescription_impl();
                TypeDescription td = tsd.addType(type.getName(), "", CAS.TYPE_NAME_ANNOTATION);
                AnnotationLayer attachType = type.getAttachType();

                td.addFeature("Dependent", "", attachType.getName());
                td.addFeature("Governor", "", attachType.getName());

                List<AnnotationFeature> features = annotationService.listAnnotationFeature(type);
                for (AnnotationFeature feature : features) {
                    generateFeature(tsd, td, feature);
                }

                types.add(tsd);
            }
            else if (type.getType().equals(CHAIN_TYPE) && !type.isBuiltIn()) {
                TypeSystemDescription tsdchains = new TypeSystemDescription_impl();
                TypeDescription tdChains = tsdchains.addType(type.getName() + "Chain", "",
                        CAS.TYPE_NAME_ANNOTATION);
                tdChains.addFeature("first", "", type.getName() + "Link");
                types.add(tsdchains);

                TypeSystemDescription tsdLink = new TypeSystemDescription_impl();
                TypeDescription tdLink = tsdLink.addType(type.getName() + "Link", "",
                        CAS.TYPE_NAME_ANNOTATION);
                tdLink.addFeature("next", "", type.getName() + "Link");
                tdLink.addFeature("referenceType", "", CAS.TYPE_NAME_STRING);
                tdLink.addFeature("referenceRelation", "", CAS.TYPE_NAME_STRING);
                types.add(tsdLink);
            }
        }

        return types;
    }
    
    private void generateFeature(TypeSystemDescription aTSD, TypeDescription aTD,
            AnnotationFeature aFeature)
    {
        switch (aFeature.getMultiValueMode()) {
        case NONE:
            aTD.addFeature(aFeature.getName(), "", aFeature.getType());
            break;
        case ARRAY: {
            switch (aFeature.getLinkMode()) {
            case WITH_ROLE: {
                // Link type
                TypeDescription linkTD = aTSD.addType(aFeature.getLinkTypeName(), "", CAS.TYPE_NAME_TOP);
                linkTD.addFeature(aFeature.getLinkTypeRoleFeatureName(), "", CAS.TYPE_NAME_STRING);
                linkTD.addFeature(aFeature.getLinkTypeTargetFeatureName(), "", aFeature.getType());
                // Link feature
                aTD.addFeature(aFeature.getName(), "", CAS.TYPE_NAME_FS_ARRAY, linkTD.getName(), false);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported link mode ["
                        + aFeature.getLinkMode() + "] on feature [" + aFeature.getName() + "]");
            }
            break;
        }
        default:
            throw new IllegalArgumentException("Unsupported multi-value mode [" + aFeature.getMultiValueMode()
                    + "] on feature [" + aFeature.getName() + "]");
        }
    }

    @Override
    @Transactional
    public void createAutomationStatus(AutomationStatus aStatus)
    {
        entityManager.persist(aStatus);

    }

    @Override
    public boolean existsAutomationStatus(MiraTemplate aTemplate)
    {
        try {
            entityManager
                    .createQuery("FROM AutomationStatus WHERE template =:template",
                            AutomationStatus.class).setParameter("template", aTemplate)
                    .getSingleResult();
            return true;
        }
        catch (NoResultException ex) {
            return false;

        }
    }

    @Override
    public AutomationStatus getAutomationStatus(MiraTemplate aTemplate)
    {
        return entityManager
                .createQuery("FROM AutomationStatus WHERE template =:template",
                        AutomationStatus.class).setParameter("template", aTemplate)
                .getSingleResult();

    }

    /**
     * Check if a TAB-Sep training file is in correct format before importing
     */
    private boolean isTabSepFileFormatCorrect(File aFile)
    {
        try {
            LineIterator it = new LineIterator(new FileReader(aFile));
            while (it.hasNext()) {
                String line = it.next();
                if (line.trim().length() == 0) {
                    continue;
                }
                if (line.split("\t").length != 2) {
                    return false;
                }
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * A Helper method to add {@link TagsetDescription} to {@link CAS}
     * 
     * @param aCas the CAA.
     * @param aLayer the layer.
     * @param aTagSetName the tagset.
     */
    public static void updateCasWithTagSet(CAS aCas, String aLayer, String aTagSetName)
    {
        Type TagsetType = CasUtil.getType(aCas, TagsetDescription.class);
        Feature layerFeature = TagsetType.getFeatureByBaseName("layer");
        Feature nameFeature = TagsetType.getFeatureByBaseName("name");

        boolean tagSetModified = false;
        // modify existing tagset Name
        for (FeatureStructure fs : CasUtil.select(aCas, TagsetType)) {
            String layer = fs.getStringValue(layerFeature);
            String tagSetName = fs.getStringValue(nameFeature);
            if (layer.equals(aLayer)) {
                // only if the tagset name is changed
                if (!aTagSetName.equals(tagSetName)) {
                    fs.setStringValue(nameFeature, aTagSetName);
                    aCas.addFsToIndexes(fs);
                }
                tagSetModified = true;
                break;
            }
        }
        if (!tagSetModified) {
            FeatureStructure fs = aCas.createFS(TagsetType);
            fs.setStringValue(layerFeature, aLayer);
            fs.setStringValue(nameFeature, aTagSetName);
            aCas.addFsToIndexes(fs);
        }
    }
    
    @Override
    public List<Project> listAccessibleProjects()
    {
        List<Project> allowedProject = new ArrayList<Project>();

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = getUser(username);

        List<Project> allProjects = listProjects();
        List<Authority> authorities = listAuthorities(user);

        // if global admin, show all projects
        for (Authority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                return allProjects;
            }
        }

        // else only projects she is admin of
        for (Project project : allProjects) {
            if (SecurityUtil.isProjectAdmin(project, this, user)) {
                allowedProject.add(project);
            }
        }
        return allowedProject;
    }
    
    /**
     * Return true if there exist at least one annotation document FINISHED for annotation for this
     * {@link SourceDocument}
     * 
     * @param aSourceDocument
     *            the source document.
     * @param aUser
     *            the user.
     * @param aProject
     *            the project.
     * @return if a finished document exists.
     */
    @Override
    public boolean existFinishedDocument(
            SourceDocument aSourceDocument, User aUser, Project aProject)
    {
        List<de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument> annotationDocuments = listAnnotationDocuments(aSourceDocument);
        boolean finishedAnnotationDocumentExist = false;
        for (de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument annotationDocument : annotationDocuments) {
            if (annotationDocument.getState().equals(AnnotationDocumentState.FINISHED)) {
                finishedAnnotationDocumentExist = true;
                break;
            }
        }
        return finishedAnnotationDocumentExist;
    }
    
    private static void writeSerializedCas(JCas aJCas, File aFile)
        throws IOException
    {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(aFile))) {
            CASCompleteSerializer serializer = serializeCASComplete(aJCas.getCasImpl());
            os.writeObject(serializer);
        }
    }

    private static void readSerializedCas(JCas aJCas, File aFile)
        throws IOException
    {
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(aFile))) {
            CASCompleteSerializer serializer = (CASCompleteSerializer) is.readObject();
            deserializeCASComplete(serializer, aJCas.getCasImpl());
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
    
    private String getLogUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        else {
            return "SYSTEM";
        }
    }
}
