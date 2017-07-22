/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.http.test.client;

import java.io.*;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import static org.apache.http.util.Args.notNull;

class InternalFileEntity extends AbstractHttpEntity implements Cloneable {

    private final File file;

    public InternalFileEntity(final File file, final ContentType contentType) {
        super();
        this.file = notNull(file, "File");
        if (contentType!=null) {
            setContentType(contentType.toString());
        }
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return this.file.length();
    }

    @Override
    public InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        notNull(outstream, "Output stream");
        final InputStream instream = new FileInputStream(this.file);
        try {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp))!=-1) {
                outstream.write(tmp, 0, l);
            }
            outstream.flush();
        } finally {
            instream.close();
        }
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

}
