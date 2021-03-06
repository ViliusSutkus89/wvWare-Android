/*
 * InstrumentedTests.java
 *
 * Copyright (C) 2020 Vilius Sutkus'89 <ViliusSutkus89@gmail.com>
 *
 * wvWare-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viliussutkus89.android.wvware;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.viliussutkus89.android.assetextractor.AssetExtractor;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTests {

  // Files must be placed in androidTest/assets/
  private final String[] m_DOCsToTest = new String[] {
    "sample.doc",
    "Tom Taschauer.doc"
  };

  private File extractAssetDOC(String filename) {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    Context appContext = instrumentation.getTargetContext();
    Context ctx = instrumentation.getContext();

    File outputDir = new File(appContext.getCacheDir(), "DOCs-extracted-from-assets");
    File file = new AssetExtractor(ctx.getAssets()).setOverwrite().extract(outputDir, filename);
    assertNotNull("Failed to extract DOC", file);
    return file;
  }

  @Test
  public void testAllSuppliedDOCs() {
    wvWare converter = new wvWare(InstrumentationRegistry.getInstrumentation().getTargetContext());
    for (String i: m_DOCsToTest) {
      File docFile = extractAssetDOC(i);
      converter.setInputDOC(docFile);
      try {
        File htmlFile = converter.convertToHTML();
        assertTrue("Converted HTML file not found! " + i, htmlFile.exists());
        assertTrue("Converted HTML file empty! " + i, htmlFile.length() > 0);
        htmlFile.delete();
      } catch (wvWare.ConversionFailedException | IOException e) {
        e.printStackTrace();
        fail("Failed to convert DOC to HTML: " + i);
      } finally {
        docFile.delete();
      }
    }
  }

  @Test
  public void testAllSuppliedDOCsInNoGraphicsMode() {
    wvWare converter = new wvWare(InstrumentationRegistry.getInstrumentation().getTargetContext())
      .setNoGraphicsMode();

    for (String i: m_DOCsToTest) {
      File docFile = extractAssetDOC(i);
      converter.setInputDOC(docFile);
      try {
        File htmlFile = converter.convertToHTML();
        assertTrue("Converted HTML file not found! " + i, htmlFile.exists());
        assertTrue("Converted HTML file empty! " + i, htmlFile.length() > 0);
        htmlFile.delete();
      } catch (wvWare.ConversionFailedException | IOException e) {
        e.printStackTrace();
        fail("Failed to convert DOC to HTML: " + i);
      } finally {
        docFile.delete();
      }
    }
  }

  @Test
  public void HtmlFileLargerWithImageEmbedded() {
    wvWare converter = new wvWare(InstrumentationRegistry.getInstrumentation().getTargetContext());

    File docFile = extractAssetDOC("sample.doc");
    converter.setInputDOC(docFile);
    try {
      File html = converter.convertToHTML();
      File htmlNoGraphics = converter.setNoGraphicsMode().convertToHTML();
      assertTrue(html.length() > htmlNoGraphics.length());
    } catch (wvWare.ConversionFailedException | IOException e) {
      e.printStackTrace();
      fail();
    } finally {
      docFile.delete();
    }
  }

  @Test
  public void encryptedDOC() {
    wvWare converter = new wvWare(InstrumentationRegistry.getInstrumentation().getTargetContext());

    File docFile = extractAssetDOC("passwordProtected.doc");
    converter.setInputDOC(docFile);
    try {
      converter.convertToHTML();
      fail("Conversion succeeded when it should have failed because of no password!");
    } catch (wvWare.PasswordRequiredException ignored) {
    } catch (wvWare.ConversionFailedException | IOException e) {
      e.printStackTrace();
      fail();
    }

    try {
      converter.setPassword("Some wrong password").convertToHTML();
      fail("Conversion succeeded when it should have failed because of wrong password!");
    } catch (wvWare.WrongPasswordException ignored) {
    } catch (wvWare.ConversionFailedException | IOException e) {
      e.printStackTrace();
      fail();
    }

    try {
      converter.setPassword("toc").convertToHTML().delete();
    } catch (wvWare.ConversionFailedException | IOException e) {
      e.printStackTrace();
      fail();
    }

    docFile.delete();
  }

}
