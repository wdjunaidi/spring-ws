/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.schema.xsd;

import org.springframework.xml.schema.Schema;

/**
 * Represents an abstraction for an XSD schema. An XSD schema is made up of one or more schema documents.
 *
 * @author Arjen Poutsma
 * @see XsdSchemaDocument
 * @since 1.0.2
 */
public interface XsdSchema extends Schema {

    /** Returns the {@link XsdSchemaDocument} objects this schema consists of. */
    XsdSchemaDocument[] getSchemaDocuments();

}