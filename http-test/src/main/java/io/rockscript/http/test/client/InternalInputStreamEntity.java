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

import static java.lang.Math.min;
import static org.apache.http.util.Args.notNull;

class InternalInputStreamEntity extends AbstractHttpEntity {

    private final InputStream content;
    private final long length;

    public InternalInputStreamEntity(final InputStream instream, final long length, final ContentType contentType) {
        super();
        this.content = notNull(instream, "Source input stream");
        this.length = length;
        if (contentType!=null) {
            setContentType(contentType.toString());
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return this.length;
    }

    @Override
    public InputStream getContent() throws IOException {
        return this.content;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        notNull(outstream, "Output stream");
        final InputStream instream = this.content;
        try {
            final byte[] buffer = new byte[4096];
            int l;
            if (this.length<0) {
                // consume until EOF
                while ((l = instream.read(buffer))!=-1) {
                    outstream.write(buffer, 0, l);
                }
            } else {
                // consume no more than length
                long remaining = this.length;
                while (remaining>0) {
                    l = instream.read(buffer, 0, (int) min(4096, remaining));
                    if (l==-1) {
                        break;
                    }
                    outstream.write(buffer, 0, l);
                    remaining -= l;
                }
            }
        } finally {
            instream.close();
        }
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

}
