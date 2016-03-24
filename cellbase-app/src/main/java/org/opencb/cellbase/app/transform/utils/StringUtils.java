/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.app.transform.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class StringUtils {

    public static byte[] gzip(String text) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
        try {
            bufos.write(text.getBytes());
        } finally {
            bufos.close();
        }
        byte[] retval = bos.toByteArray();
        bos.close();
        return retval;
    }

    public static String gunzip(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = bufis.read(buffer)) >= 0) {
            bos.write(buffer, 0, len);
        }
        String retval = bos.toString();
        bis.close();
        bufis.close();
        bos.close();
        return retval;
    }

}
