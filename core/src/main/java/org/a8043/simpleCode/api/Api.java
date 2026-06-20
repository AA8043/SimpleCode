package org.a8043.simpleCode.api;

import org.a8043.simpleCode.model.Model;
import org.a8043.simpleCode.model.Provider;
import org.a8043.simpleCode.model.RemoteModel;
import org.a8043.simpleCode.session.Session;

import java.util.List;

public interface Api {
    CompleteResult complete(Model model, Session session);

    List<RemoteModel> getModels(Provider provider);
}
