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
package com.anrisoftware.dwarfhustle.model.db.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Slf4j
public class LuceneBenchmark {

    @SneakyThrows
    public static void main(String[] args) {
        Analyzer analyzer = new StandardAnalyzer();
        Path indexPath = Files.createTempDirectory("tempIndex");
        Directory directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);

        log.debug("Start add documents");
        for (int i = 0; i < 500000; i++) {
            Document doc = new Document();
            String text = "carpender-" + i;
            long id = i;
            doc.add(new LongField("id", id, Store.YES));
            doc.add(new StringField("name", text, Store.YES));
            iwriter.addDocument(doc);
        }
        log.debug("end add documents");
        iwriter.close();

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":

        val rnd1 = new Random();
        log.debug("Start by term search");
        for (int i = 0; i < 5000; i++) {
            Term term = new Term("name", "carpender-" + rnd1.nextLong(0, 5000));
            Query query = new TermQuery(term);
            ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
            assertEquals(1, hits.length);
        }
        log.debug("end by term search");

        val rnd2 = new Random();
        log.debug("Start by id search");
        for (int i = 0; i < 5000; i++) {
            Query query = LongField.newExactQuery("id", rnd2.nextLong(0, 5000));
            val hits = isearcher.search(query, 10).scoreDocs;
            assertEquals(1, hits.length);
        }
        log.debug("end by id search");

        ireader.close();
        directory.close();
        IOUtils.rm(indexPath);

    }
}
