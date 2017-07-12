package org.daisy.dotify.impl.input.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystemException;

public class DocsGroup implements TaskGroup {
	private final TaskGroupSpecification specification;
	
	DocsGroup(TaskGroupSpecification specification) {
		this.specification = specification;
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters)
			throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		if ("docx".equalsIgnoreCase(specification.getInputFormat())) {
			ret.add(new DocxTask(parameters));
		} else if ("odt".equalsIgnoreCase(specification.getInputFormat())) {
			ret.add(new OdtTask(parameters));
		}
		return ret;
	}

}