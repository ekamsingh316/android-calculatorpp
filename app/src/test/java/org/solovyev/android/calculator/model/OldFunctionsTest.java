/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.model;

import jscl.util.ExpressionGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.solovyev.android.calculator.functions.OldFunctions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Random;

/**
 * User: serso
 * Date: 11/14/12
 * Time: 8:06 PM
 */
public class OldFunctionsTest {

    private static final String xml = "<functions>\n" +
            "   <functions class=\"java.util.ArrayList\">\n" +
            "      <function>\n" +
            "         <name>test</name>\n" +
            "         <body>x+y</body>\n" +
            "         <parameterNames class=\"java.util.ArrayList\">\n" +
            "            <string>x</string>\n" +
            "            <string>y</string>\n" +
            "         </parameterNames>\n" +
            "         <system>false</system>\n" +
            "         <description>description</description>\n" +
            "      </function>\n" +
            "      <function>\n" +
            "         <name>z_2</name>\n" +
            "         <body>e^(z_1^2+z_2^2)</body>\n" +
            "         <parameterNames class=\"java.util.ArrayList\">\n" +
            "            <string>z_1</string>\n" +
            "            <string>z_2</string>\n" +
            "         </parameterNames>\n" +
            "         <system>true</system>\n" +
            "         <description></description>\n" +
            "      </function>\n" +
            "      <function>\n" +
            "         <name>z_2</name>\n" +
            "         <body>e^(z_1^2+z_2^2)</body>\n" +
            "         <parameterNames class=\"java.util.ArrayList\">\n" +
            "            <string>z_1</string>\n" +
            "            <string>z_2</string>\n" +
            "         </parameterNames>\n" +
            "         <system>true</system>\n" +
            "         <description></description>\n" +
            "      </function>\n" +
            "   </functions>\n" +
            "</functions>";

    @Nonnull
    private OldFunctions testXml(@Nonnull OldFunctions in, @Nullable String expectedXml) throws Exception {
        final String actualXml = toXml(in);

        if (expectedXml != null) {
            Assert.assertEquals(expectedXml, actualXml);
        }

        final Serializer serializer = new Persister();
        final OldFunctions out = serializer.read(OldFunctions.class, actualXml);
        final String actualXml2 = toXml(out);
        Assert.assertEquals(actualXml, actualXml2);
        return out;
    }

    private String toXml(OldFunctions in) throws Exception {
        final StringWriter sw = new StringWriter();
        final Serializer serializer = new Persister();
        serializer.write(in, sw);
        return sw.toString();
    }

    @Test
    public void testRandomXml() throws Exception {
        final OldFunctions in = new OldFunctions();

        final Random random = new Random(new Date().getTime());

        ExpressionGenerator generator = new ExpressionGenerator(10);
        for (int i = 0; i < 1000; i++) {
/*            final String content = generator.generate();

            final String paramsString = Strings.generateRandomString(random.nextInt(10));
            final List<String> parameterNames = new ArrayList<String>();
            for (int j = 0; j < paramsString.length(); j++) {
                parameterNames.add(String.valueOf(paramsString.charAt(j)));
            }

            final OldFunction.Builder builder = new OldFunction.Builder("test_" + i, content, parameterNames);

            if (random.nextBoolean()) {
                builder.setDescription(Strings.generateRandomString(random.nextInt(100)));
            }

            builder.setSystem(random.nextBoolean());

            in.getEntities().add(builder.create());*/
        }

        testXml(in, null);
    }

}
