package com.joshlong.cleanvoice.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Cleanvoice returns {@code []} instead of {@code null} for object fields that weren't
 * requested or aren't ready yet (e.g. {@code summarization}, {@code transcription}). This
 * reads an empty array as {@code null} and anything else as the property's declared type.
 */
class EmptyArrayAsNullDeserializer extends ValueDeserializer<Object> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final JavaType type;

	private final String name;

	EmptyArrayAsNullDeserializer() {
		this(null, null);
	}

	private EmptyArrayAsNullDeserializer(JavaType type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
		return new EmptyArrayAsNullDeserializer(property.getType(), property.getName());
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) {
		if (p.currentToken() == JsonToken.START_ARRAY) {
			if (p.nextToken() == JsonToken.END_ARRAY) {
				this.log.debug("'{}' came back as [] (not requested or not ready); reading it as null", this.name);
				return null;
			}
			return ctxt.reportInputMismatch(this.type, "Expected an object or an empty array for %s", this.type);
		}
		return ctxt.readValue(p, this.type);
	}

}
