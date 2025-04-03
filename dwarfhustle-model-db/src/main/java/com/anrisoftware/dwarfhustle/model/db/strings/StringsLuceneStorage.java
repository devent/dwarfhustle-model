/*
 * dwarfhustle-model-db - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.db.strings;

import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.eclipse.collections.api.factory.Lists;

import com.anrisoftware.dwarfhustle.model.api.objects.GameObject;
import com.anrisoftware.dwarfhustle.model.api.objects.GameObjectsStorage;
import com.anrisoftware.dwarfhustle.model.api.objects.StringObject;
import com.google.inject.assistedinject.Assisted;

import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Stores {@link StringObject}(s).
 */
public class StringsLuceneStorage implements GameObjectsStorage {

    /**
     * Factory to create the {@link StringsLuceneStorage}.
     *
     * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
     */
    public interface StringsLuceneStorageFactory {
        StringsLuceneStorage create(Path file);
    }

    private IndexWriter writer;

    private ReaderManager reader;

    private SearcherManager searcher;

    /**
     * Creates or opens the game objects storage.
     */
    @Inject
    @SneakyThrows
    public StringsLuceneStorage(@Assisted Path file) {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = MMapDirectory.open(file);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.writer = new IndexWriter(directory, config);
        writer.commit();
        this.reader = new ReaderManager(directory);
        this.searcher = new SearcherManager(directory, null);
    }

    /**
     * Closes the storage.
     */
    @Override
    @SneakyThrows
    public void close() {
        writer.close();
        reader.close();
        searcher.close();
    }

    @SneakyThrows
    public StringObject getObject(int type, long key) {
        searcher.maybeRefresh();
        reader.maybeRefresh();
        val o = new StringObject();
        val s = searcher.acquire();
        try {
            Query query = LongField.newExactQuery("id", key);
            val topdocs = s.search(query, 1);
            if (topdocs.totalHits.value() == 0) {
                return o;
            }
            int docId = topdocs.scoreDocs[0].doc;
            val r = reader.acquire();
            try {
                val fields = r.storedFields();
                val doc = fields.document(docId);
                o.setId(key);
                o.setS(doc.get("name"));
            } finally {
                reader.release(r);
            }
        } finally {
            searcher.release(s);
        }
        return o;
    }

    @SneakyThrows
    public void addObject(StringObject go) {
        try {
            Document doc = new Document();
            doc.add(new LongField("id", go.getId(), Store.YES));
            doc.add(new StringField("name", go.getS(), Store.YES));
            writer.addDocument(doc);
            writer.commit();
        } catch (Exception e) {
            writer.rollback();
        }
    }

    @SneakyThrows
    public void addObject(Iterable<StringObject> gos) {
        try {
            for (StringObject go : gos) {
                Document doc = new Document();
                doc.add(new LongField("id", go.getId(), Store.YES));
                doc.add(new StringField("name", go.getS(), Store.YES));
                writer.addDocument(doc);
            }
            writer.commit();
        } catch (Exception e) {
            writer.rollback();
        }
    }

    @SneakyThrows
    public void setObject(StringObject go) {
        searcher.maybeRefresh();
        reader.maybeRefresh();
        val s = searcher.acquire();
        TopDocs topdocs = null;
        try {
            Query query = LongField.newExactQuery("id", go.getId());
            topdocs = s.search(query, 1);
        } finally {
            searcher.release(s);
        }
        if (topdocs.totalHits.value() == 0) {
            try {
                Document doc = new Document();
                doc.add(new LongField("id", go.getId(), Store.YES));
                doc.add(new StringField("name", go.getS(), Store.YES));
                writer.addDocument(doc);
                writer.commit();
            } catch (Exception e) {
                writer.rollback();
            }
            return;
        }
        val r = reader.acquire();
        try {
            Document doc = new Document();
            doc.add(new LongField("id", go.getId(), Store.YES));
            doc.add(new StringField("name", go.getS(), Store.YES));
            writer.updateDocuments(LongField.newExactQuery("id", go.getId()), Lists.fixedSize.of(doc));
            writer.commit();
        } finally {
            reader.release(r);
        }

    }

    @SneakyThrows
    public void removeObject(int type, long id) {
        searcher.maybeRefresh();
        reader.maybeRefresh();
        writer.deleteDocuments(LongField.newExactQuery("id", id));
        writer.commit();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GameObject> T get(int type, long key) {
        return (T) getObject(type, key);
    }

    @Override
    public void set(int type, GameObject go) throws ObjectsSetterException {
        setObject((StringObject) go);
    }

    @Override
    public void set(int type, Iterable<GameObject> values) throws ObjectsSetterException {
    }

    @Override
    public void remove(int type, GameObject go) throws ObjectsSetterException {
        removeObject(type, go.getId());
    }

    @Override
    public void remove(int type, long id) throws ObjectsSetterException {
        removeObject(type, id);
    }

}
