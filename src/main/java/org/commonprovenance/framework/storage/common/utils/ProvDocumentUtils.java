package org.commonprovenance.framework.storage.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.model.interop.InteropMediaType;

public class ProvDocumentUtils {
  public static final Charset charset = java.nio.charset.StandardCharsets.UTF_8;

  public static String serialize(Document document, Formats.ProvFormat format) {
    InteropFramework interop = new InteropFramework();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    interop.writeDocument(outputStream, document, ProvFormatToIntermediaType(format), false);
    String serialized = outputStream.toString(charset);
    return finalizeAfterSerialization(serialized, format);
  }

  public static String finalizeAfterSerialization(String serializedDocument, Formats.ProvFormat format) {
    if (format == Formats.ProvFormat.JSON) {
      serializedDocument = ProvJsonUtils.postprocessJsonAfterSerialization(serializedDocument);
    }

    return serializedDocument;
  }

  public static Document deserialize(String ser, Formats.ProvFormat format) throws IOException {
    String s = prepareForDeserialization(ser, format);
    InteropFramework interop = new InteropFramework();
    InputStream inputStream = new ByteArrayInputStream(BytesUtils.stringToBytes_UTF8(s));
    return interop.readDocument(inputStream, format);
  }

  public static String prepareForDeserialization(String serializedDocument, Formats.ProvFormat format) {
    if (format == Formats.ProvFormat.JSON) {
      serializedDocument = ProvJsonUtils.preprocessJsonForDeserialization(serializedDocument);
    }

    return serializedDocument;
  }

  public static String ProvFormatToIntermediaType(Formats.ProvFormat format) {
    return switch (format) {
      case JSON -> InteropMediaType.MEDIA_APPLICATION_JSON;
      case JSONLD -> InteropMediaType.MEDIA_APPLICATION_JSONLD;
      case PROVN -> InteropMediaType.MEDIA_TEXT_PROVENANCE_NOTATION;
      case TURTLE -> InteropMediaType.MEDIA_TEXT_TURTLE;
      case TRIG -> InteropMediaType.MEDIA_APPLICATION_TRIG;
      case RDFXML -> InteropMediaType.MEDIA_APPLICATION_XML;
      default -> throw new IllegalStateException("Switch case for: " + format + " is not defined.");
    };
  }
}
