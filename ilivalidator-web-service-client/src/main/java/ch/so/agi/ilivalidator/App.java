package ch.so.agi.ilivalidator;

import static elemental2.dom.DomGlobal.console;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;

import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.FormData.AppendValueUnionType;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDocument;
import elemental2.dom.HTMLFormElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLOptionsCollection;
import elemental2.dom.HTMLSelectElement;
import elemental2.dom.RequestInit;
import elemental2.dom.URL;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsForEachCallbackFn;
import jsinterop.base.JsPropertyMap;


public class App implements EntryPoint {

    // Kann auch via Settings vom Server kommen.
    // Wäre momentan aber einziges Setting vom Server. Momentan sein lassen.
    private static int MAX_FILES_SIZE_MB = 200; 

    private static final String API_ENDPOINT_JOBS = "/api/jobs";
    private static final String API_ENDPOINT_PROFILES = "/api/profiles";
    private static final String HEADER_OPERATION_LOCATION = "Operation-Location";

    private HTMLFormElement form;
    private HTMLSelectElement select;
    private HTMLInputElement input;
    private HTMLButtonElement button;
    
    private String host; 
    private String protocol;
    private String pathname;
    
    private Map<String,String> profiles;

	public void onModuleLoad() {
        console.log("Hallo Stefan");
        
        URL url = new URL(DomGlobal.location.href);
        host = url.host;
        protocol = url.protocol;
        pathname = url.pathname.length()==1?"":url.pathname;
        
        String requestUrl = protocol + "//" + host + pathname + API_ENDPOINT_PROFILES;
        
        DomGlobal.fetch(requestUrl).then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert(response.statusText + ": " + response.body);
                return null;
            }
            return response.text();
        }).then(json -> {            
            JsPropertyMap<Object> responseMap = Js.asPropertyMap(Global.JSON.parse(json));
            JsPropertyMap<Object> profilesMap = Js.asPropertyMap(responseMap.get("profiles"));
            profiles = new HashMap<>();
            profilesMap.forEach(new JsForEachCallbackFn() {
                @Override
                public void onKey(String key) {
                    String value = String.valueOf(profilesMap.get(key));
                    profiles.put(key, value);
                }                
            });
            
            init();
            
            return null;
        }).catch_(error -> {
            console.log(error);
            DomGlobal.window.alert(error.toString());
            return null;
        });

        
        
//        init();
        //body().add(div().textContent("Hallo Welt"));
	}
	
	private void init() {
	    select = (HTMLSelectElement) getDocument().getElementById("profileSelect");
        HTMLOptionsCollection options = select.options;

        for (Map.Entry<String, String> entry : profiles.entrySet()) {
            HTMLOptionElement option = (HTMLOptionElement) getDocument().createElement("option");        
            option.text = entry.getKey();
            option.value = entry.getValue();
            options.add(option);            
        }        

        select.options = options;
        
        select.addEventListener("change", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                String theme = select.selectedOptions.getAt(0).text;
                if (select.selectedIndex == 0) {
                    updateUrlLocation(null);
                } else {
                    updateUrlLocation(theme);
                }                
            }
        });

        
	    
	    button = (HTMLButtonElement) getDocument().getElementById("submitButton");
//        button.addEventListener("click", (Event event) -> {
//            
//            console.log(host);
//            console.log(pathname);
//
//            
//            
//            console.log("Button clicked");
//            Window.alert("Hello World!");
//        });
        
	    input = (HTMLInputElement) getDocument().getElementById("fileInput");
        
        form = (HTMLFormElement) getDocument().getElementById("uploadForm");
        form.addEventListener("submit", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                evt.preventDefault();

                if (input.files.length < 1) {
                    return;
                }
                                
                input.disabled = true;
                button.disabled = true;

                FormData formData = new FormData();

                String jobId = uuid();
                formData.append("jobId", jobId);
                
                formData.append("profile", select.selectedOptions.getAt(0).value);
                
                List<String> fileNames = new ArrayList<>();
                int filesSize = 0;
                for (int i=0; i<input.files.length; i++) {
                    File file = input.files.getAt(i);
                    fileNames.add(file.name);
                    // Falls Safari Probleme macht:
                    // https://github.com/hal/console/blob/d435e24a837adedbd6aefe06495eafaa97c08a65/dmr/src/main/java/org/jboss/hal/dmr/dispatch/Dispatcher.java#L263
                    formData.append("files", AppendValueUnionType.of(file), file.name);
                    filesSize += file.size;
                    int filesSizeMb = filesSize / 1024 / 1024;
                    if (filesSizeMb > MAX_FILES_SIZE_MB) {
                        //logToProtocol(messages.errorTooLargeFile(String.valueOf(MAX_FILES_SIZE_MB)));
                        Window.alert("Datei(en) zu gross. Maximum: " + String.valueOf(MAX_FILES_SIZE_MB) + "MB");
                        resetInputElements();
                        return;
                    }
                }
                
                for (String fileName : fileNames) {
                    //logToProtocol(messages.uploadFile(fileName));
                    console.log(fileName);
                }

                
                RequestInit init = RequestInit.create();
                init.setMethod("POST");
                init.setBody(formData);

                String requestUrl = protocol + "//" + host + pathname + API_ENDPOINT_JOBS;

                DomGlobal.fetch(requestUrl, init)
                .then(response -> {
                    if (!response.ok) {
                        resetInputElements();
                        
                        Promise<String> foo  = response.text();
                        foo.then(r -> { 
                            //logToProtocol(r);
                            console.log(r);
                            return null;
                        });
                        
                        return null;
                    }
                    String jobUrl = response.headers.get(HEADER_OPERATION_LOCATION);
                    return null;
                })
                .catch_(error -> {
                    console.log(error);
                    //logToProtocol(error.toString());
                    resetInputElements();
                    return null;
                });
            }
            
        });

	}
	
    private void resetInputElements() {
        form.reset();
        input.disabled = false;
        button.disabled = false;
        //button.textContent = messages.submitButtonDefault();
    }

    private HTMLDocument getDocument() {
        return DomGlobal.document;
    }
    
    private void updateUrlLocation(String theme) {
        URL url = new URL(DomGlobal.location.href);
        
        String newUrl = protocol + "//" + host + pathname;
        if (theme != null) {
            URLSearchParams params = url.searchParams;
            params.set("p", theme);
            newUrl += "?" + params.toString(); 
        } 
        updateUrlWithoutReloading(newUrl);
    }

    // Update the URL in the browser without reloading the page.
    private static native void updateUrlWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
    
    public native static String uuid() /*-{
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
            function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r
                        : (r & 0x3 | 0x8);
                return v.toString(16);
            });
    }-*/;
}
