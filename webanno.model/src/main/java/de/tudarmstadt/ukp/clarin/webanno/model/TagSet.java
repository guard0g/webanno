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
package de.tudarmstadt.ukp.clarin.webanno.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;
/**
 * A persistence object for a TagSet
 * @author Seid Muhie Yimam
 *
 */
@Entity
@Table(name = "tag_set", uniqueConstraints = { @UniqueConstraint(columnNames = { "name","project" }) })
public class TagSet
    implements Serializable
{
    private static final long serialVersionUID = 5644063605817006810L;

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "project")
    Project project;

    @ManyToOne
    @ForeignKey(name = "none")
    @JoinColumn(name = "annotation_type")
    AnnotationType layer;

    @ManyToOne
    @ForeignKey(name = "none")
    @JoinColumn(name = "annotation_feature")
    AnnotationFeature feature;

    @Column(nullable = false)
    private String name;

    private String language;

    @Lob
    private String description;

    boolean createTag = true;

    public long getId()
    {
        return id;
    }

    public void setId(long aId)
    {
        id = aId;
    }


    public AnnotationFeature getFeature()
    {
        return feature;
    }

    public void setFeature(AnnotationFeature feature)
    {
        this.feature = feature;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String aLanguage)
    {
        language = aLanguage;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String aDescription)
    {
        description = aDescription;
    }


    public Project getProject()
    {
        return project;
    }

    public void setProject(Project aProject)
    {
        project = aProject;
    }



    public AnnotationType getLayer()
    {
        return layer;
    }

    public void setLayer(AnnotationType layer)
    {
        this.layer = layer;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TagSet other = (TagSet) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        }
        else if (!project.equals(other.project)) {
            return false;
        }
        return true;
    }

    public boolean isShowTag()
    {
        return createTag;
    }

    public void setSHowTag(boolean createTag)
    {
        this.createTag = createTag;
    }

}
