package org.daisy.dotify.impl.input.docs;

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
		tmp.add(TaskGroupInformation.newConvertBuilder("docx", "html").build());
		tmp.add(TaskGroupInformation.newConvertBuilder("odt", "html").build());
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
