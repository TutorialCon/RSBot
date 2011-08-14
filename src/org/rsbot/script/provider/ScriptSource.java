package org.rsbot.script.provider;

import org.rsbot.script.Script;

import java.util.List;

/**
 */
public interface ScriptSource {

	List<ScriptDefinition> list();

	Script load(ScriptDefinition def) throws InstantiationException, IllegalAccessException;

}
