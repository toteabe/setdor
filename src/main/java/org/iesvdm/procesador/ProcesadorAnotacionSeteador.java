package org.iesvdm.procesador;

import org.iesvdm.anotacion.Seteador;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({"org.iesvdm.anotacion.Seteador"}) // nombre de anotacion con ruta de paquete completa
@SupportedSourceVersion(SourceVersion.RELEASE_8) // indica la minima version de java soportada para la anotacion que procesa
public class ProcesadorAnotacionSeteador extends AbstractProcessor {

    private Messager messager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // obtenemos elementos anotados con la anotacion @Seteador
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(Seteador.class);

        for (Element element : annotatedElements) {
            if (element.getKind() == ElementKind.METHOD) {
                // only handle methods as targets
                checkMethod((ExecutableElement) element);
            }
        }

        // false -> subsiguientes procesadores pueden tratarlo
        // true -> no se permite que subsiguientes procesadores puedan tratar esta anotacion
        return false;
    }

    private void checkMethod(ExecutableElement method) {
        // chequear formato de nombre correcto
        String name = method.getSimpleName().toString();
        if (!name.startsWith("set")) {
            printError(method, "seteador debe empezar con \"set\"");
        } else if (name.length() == 3) {
            printError(method, "el nombre del método debe contener algo más que sólo \"set\"");
        } else if (Character.isLowerCase(name.charAt(3))) {
            if (method.getParameters().size() != 1) {
                printError(method, "carácter siguiente a \"set\" debe ser en mayúscula");
            }
        }

        // chequear si el seteador es publico
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
            printError(method, "seteador debe ser público");
        }

        // chequear si el seteador es no estatico
        if (method.getModifiers().contains(Modifier.STATIC)) {
            printError(method, "setter must not be static");
        }
    }

    private void printError(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        // establecer el messager para imprimir mensajes de error
        messager = processingEnvironment.getMessager();
    }
}
