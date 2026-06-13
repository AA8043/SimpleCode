package org.a8043.simpleCode.api;

import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.session.content.Content;

import java.util.List;

public interface Api {
    CompleteResult complete(Model model, List<Content> context);
}
