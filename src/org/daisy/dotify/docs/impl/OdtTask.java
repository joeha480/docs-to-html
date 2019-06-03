package org.daisy.dotify.docs.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

import fr.opensagres.odfdom.converter.xhtml.XHTMLConverter;
import fr.opensagres.odfdom.converter.xhtml.XHTMLOptions;

public class OdtTask extends ReadWriteTask {
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String DEFAULT_LANGUAGE = Locale.getDefault().toLanguageTag();
	private static final String UTF_8 = "utf-8";
	private static List<UserOption> options = null;
	private final String language;
	private final String outputCharset;
	
	public OdtTask(Map<String, Object> params) {
		super("Odt to HTML");
		this.language = getLanguage(params);
		this.outputCharset = UTF_8;
	}

	private static String getLanguage(Map<String, Object> params) {
		Object param = params.get(SOURCE_LANGUAGE);
		return (param!=null)?""+param:DEFAULT_LANGUAGE;
	}

	@Override
	@Deprecated
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try (InputStream in = convertToHtml(input.getPath())) {
			// Using JSoup to clean up html entities
			org.jsoup.nodes.Document doc2 = Jsoup.parse(in,
					UTF_8,
					input.getPath().getParent().toUri().toASCIIString(),
					Parser.xmlParser());
			doc2.outputSettings()
				.escapeMode(EscapeMode.xhtml)
				.charset(outputCharset)
				.syntax(Syntax.xml);
			Elements html = doc2.getElementsByTag("html");
			html.attr("xmlns", "http://www.w3.org/1999/xhtml");
			html.attr("xml:lang", language);
			Files.write(output.toPath(), doc2.html().getBytes(outputCharset));
			return new DefaultAnnotatedFile.Builder(output.toPath())
					.extension("html")
					.mediaType("application/xhtml+xml")
					.build();
		} catch (IOException e) {
			throw new InternalTaskException("", e);
		}
	}

	private static InputStream convertToHtml(Path input) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (InputStream in = Files.newInputStream(input); Writer w = new OutputStreamWriter(bos, UTF_8)) {
			OdfTextDocument doc = OdfTextDocument.loadDocument(in);
			XHTMLOptions options = XHTMLOptions.create();
			XHTMLConverter.getInstance().convert(doc, w, options);
			return new ByteArrayInputStream(bos.toByteArray());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static synchronized List<UserOption> getOptionsInternal() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new UserOption.Builder(SOURCE_LANGUAGE).description("The primary language of the input file").defaultValue(DEFAULT_LANGUAGE).build());
		}
		return options;		
	}
	
	@Override
	public List<UserOption> getOptions() {
		return getOptionsInternal();
	}
}
