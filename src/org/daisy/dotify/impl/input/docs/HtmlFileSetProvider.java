package org.daisy.dotify.impl.input.docs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.media.DefaultFileSet;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.FileSet;
import org.daisy.streamline.api.media.FileSetException;
import org.daisy.streamline.api.media.FileSetProvider;

public class HtmlFileSetProvider implements FileSetProvider {

	@Override
	public boolean accepts(FileDetails type) {
		return type.getFormatName()!=null && (type.getFormatName().equalsIgnoreCase("html") || type.getFormatName().equalsIgnoreCase("xhtml")) || 
				type.getMediaType()!=null && (type.getMediaType().equalsIgnoreCase("text/html") || type.getMediaType().equalsIgnoreCase("application/xhtml+xml")) ||
				type.getExtension()!=null && (type.getExtension().equalsIgnoreCase("html") || type.getExtension().equalsIgnoreCase("xhtml"));
	}

	@Override
	public FileSet create(AnnotatedFile f, Map<String, Object> parameters) throws FileSetException {
		try {
			org.jsoup.nodes.Document doc = HtmlTask.parse(f);
			Path baseFolder = f.getPath().getParent();
			List<AnnotatedFile> resources = HtmlTask.getResources(doc, baseFolder)
				.stream()
				.map(v->DefaultAnnotatedFile.create(baseFolder.resolve(v)))
				.collect(Collectors.toList());
			return new DefaultFileSet.Builder(f, resources).build();
		} catch (IOException e) {
			throw new FileSetException(e);
		}
	}

}
