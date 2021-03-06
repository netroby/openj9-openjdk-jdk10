/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import jdk.incubator.http.internal.common.HttpHeadersImpl;

public class Http2RedirectHandler implements Http2Handler {

    final Supplier<String> supplier;

    public Http2RedirectHandler(Supplier<String> redirectSupplier) {
        supplier = redirectSupplier;
    }

    @Override
    public void handle(Http2TestExchange t) throws IOException {
        examineExchange(t);
        try (InputStream is = t.getRequestBody()) {
            is.readAllBytes();
            String location = supplier.get();
            System.err.printf("RedirectHandler request to %s from %s\n",
                t.getRequestURI().toString(), t.getRemoteAddress().toString());
            System.err.println("Redirecting to: " + location);
            HttpHeadersImpl map1 = t.getResponseHeaders();
            map1.addHeader("Location", location);
            t.sendResponseHeaders(301, 1024);
            byte[] bb = new byte[1024];
            OutputStream os = t.getResponseBody();
            os.write(bb);
            os.close();
            t.close();
        }
    }

    // override in sub-class to examine the exchange, but don't
    // alter transaction state by reading the request body etc.
    protected void examineExchange(Http2TestExchange t) {
    }
}
