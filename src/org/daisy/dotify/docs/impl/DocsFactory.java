package org.daisy.dotify.docs.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupFactory;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;

public class DocsFactory implements TaskGroupFactory {
	private final Set<TaskGroupInformation> information;
	
	public DocsFactory() {
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("docx", "xhtml").build());
		tmp.add(TaskGroupInformation.newConvertBuilder("odt", "xhtml").build());
		tmp.add(TaskGroupInformation.newConvertBuilder("html", "xhtml").build());
		information = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean supportsSpecification(TaskGroupInformation specification) {
		return information.contains(specification);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification specification) {
		return new DocsGroup(specification);
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

}
