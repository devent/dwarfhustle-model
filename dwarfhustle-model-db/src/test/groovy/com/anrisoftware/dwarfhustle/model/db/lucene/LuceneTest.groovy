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
package com.anrisoftware.dwarfhustle.model.db.lucene

import java.nio.file.Path

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.Field.Store
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.ReaderManager
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import groovy.util.logging.Slf4j

/**
 * Tests Lucene.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
class LuceneTest {

    @Test
    void use_manager(@TempDir Path temp) {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(temp);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        try {
            Document doc = new Document();
            doc.add(new LongField("id", 1, Store.YES));
            doc.add(new StringField("name", "carpender-1", Store.YES));
            iwriter.addDocument(doc)
            iwriter.commit()
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        def reader = new ReaderManager(directory)
        def searcher = new SearcherManager(directory, null)
        IndexSearcher s = searcher.acquire();
        try {
            Term term = new Term("name", "carpender-1");
            Query query = new TermQuery(term);
            ScoreDoc[] hits = s.search(query, 10).scoreDocs;
            assert hits.length == 1
        } finally {
            searcher.release(s);
        }

        try {
            Document doc = new Document();
            doc.add(new LongField("id", 2, Store.YES));
            doc.add(new StringField("name", "carpender-2", Store.YES));
            iwriter.addDocument(doc)
            iwriter.commit()
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        searcher.maybeRefresh()

        s = searcher.acquire();
        try {
            Term term = new Term("name", "carpender-2");
            Query query = new TermQuery(term);
            ScoreDoc[] hits = s.search(query, 10).scoreDocs;
            assert hits.length == 1
        } finally {
            searcher.release(s);
        }

        searcher.close()
        reader.close()
    }

    @Test
    void open_empty(@TempDir Path temp) {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(temp);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        iwriter.commit()

        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        Term term = new Term("name", "carpender-1");
        Query query = new TermQuery(term);
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 0
        ireader.close()
    }

    @Test
    void open_old_index() {
        Path temp = Path.of("/home/devent/Projects/dwarf-hustle/terrain-maps/lucene-index")
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(temp);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        iwriter.commit()

        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        Term term = new Term("name", "carpender-1");
        Query query = new TermQuery(term);
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 1
        ireader.close()
    }

    @Test
    void write_read_docs(@TempDir Path temp) {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(temp);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        try {
            Document doc = new Document();
            doc.add(new LongField("id", 1, Store.YES));
            doc.add(new StringField("name", "carpender-1", Store.YES));
            iwriter.addDocument(doc)
            iwriter.commit()
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        Term term = new Term("name", "carpender-1");
        Query query = new TermQuery(term);
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 1
        ireader.close()

        try {
            Document doc = new Document();
            doc.add(new LongField("id", 2, Store.YES));
            doc.add(new StringField("name", "carpender-2", Store.YES));
            iwriter.addDocument(doc)
            iwriter.commit()
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);

        term = new Term("name", "carpender-2");
        query = new TermQuery(term);
        hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 1
    }

    @Test
    void write_read_docs_big_sizes(@TempDir Path temp) {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = FSDirectory.open(temp);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        try {
            log.debug("Start add documents");
            for (int i = 0; i < 500000; i++) {
                Document doc = new Document();
                String text = "carpender-" + i;
                long id = i;
                doc.add(new LongField("id", id, Store.YES));
                doc.add(new StringField("name", text, Store.YES));
                iwriter.addDocument(doc);
            }
            iwriter.commit()
            log.debug("end add documents");
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        log.debug("Open index");
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        log.debug("finish open index");

        Term term = new Term("name", "carpender-1");
        Query query = new TermQuery(term);
        ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 1
        ireader.close()

        try {
            log.debug("Start add documents");
            for (int i = 500000; i < 500000 + 500000; i++) {
                Document doc = new Document();
                String text = "carpender-" + i;
                long id = i;
                doc.add(new LongField("id", id, Store.YES));
                doc.add(new StringField("name", text, Store.YES));
                iwriter.addDocument(doc);
            }
            iwriter.commit()
            log.debug("end add documents");
        } catch (e) {
            iwriter.rollback()
            log.error("", e)
        }

        log.debug("Open index");
        ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);
        log.debug("finish open index");

        term = new Term("name", "carpender-2");
        query = new TermQuery(term);
        hits = isearcher.search(query, 10).scoreDocs;
        assert hits.length == 1

        QueryParser queryParser = new QueryParser("name", analyzer);
        query = queryParser.parse("carpender*");

        // Search the index
        def topdocs = isearcher.search(query, 10);
        assert topdocs.totalHits.toString() == "1001+ hits"

        ireader.close()
    }
}
