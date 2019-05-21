package org.daisy.dotify.impl.input.docs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;

public class DocxTask extends ReadWriteTask {
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String DEFAULT_LANGUAGE = Locale.getDefault().toLanguageTag();
	private static final String DEFAULT_OUTPUT_CHARSET = "utf-8";
	private static List<UserOption> options = null;
	private final String language;
	private final String outputCharset;
	
	public DocxTask(Map<String, Object> params) {
		super("Docx to HTML");
		this.language = getLanguage(params);
		this.outputCharset = DEFAULT_OUTPUT_CHARSET;
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
		try (InputStream in = Files.newInputStream(input.getPath())) {
			XWPFDocument doc = new XWPFDocument(in);
			XHTMLOptions options = XHTMLOptions.create();	
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XHTMLConverter.getInstance().convert(doc, bos, options);
			// Using JSoup to clean up html entities (and set xmlns and xml:lang)
			org.jsoup.nodes.Document doc2 = Jsoup.parse(new ByteArrayInputStream(bos.toByteArray()),
					"utf-8",
					input.getPath().getParent().toUri().toASCIIString(),
					Parser.xmlParser());
			doc2.outputSettings()
				.escapeMode(EscapeMode.xhtml)
				.charset(outputCharset)
				.syntax(Syntax.xml);
			Elements html = doc2.getElementsByTag("html");
			html.attr("xmlns", "http://www.w3.org/1999/xhtml");
			html.attr("xml:lang", language);
			Element head = doc2.getElementsByTag("head").first();
			head.appendChild(new Element("meta").attr("charset", outputCharset));
			Files.write(output.toPath(), doc2.html().getBytes(outputCharset));
			return new DefaultAnnotatedFile.Builder(output.toPath())
					.extension("html")
					.mediaType("application/xhtml+xml")
					.build();
		} catch (IOException e) {
			throw new InternalTaskException("", e);
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
