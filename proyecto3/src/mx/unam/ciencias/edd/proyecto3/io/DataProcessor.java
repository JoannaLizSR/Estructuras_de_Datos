package mx.unam.ciencias.edd.proyecto3.io;

import mx.unam.ciencias.edd.Diccionario;
import mx.unam.ciencias.edd.Grafica;
import mx.unam.ciencias.edd.Lista;
import mx.unam.ciencias.edd.proyecto3.exception.InvalidDirectoryException;
import mx.unam.ciencias.edd.proyecto3.exception.NoArgumentsGivenException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;

/**
 * Clase la cual se encarga de hacer los algoritmos suficientes para contar las palabras,
 * identificar lo(s) archivo(s) y el directorio, y generar la gráfica entre los archivos.
 *
 * @author Angel Gladin
 * @version 1.0
 * @since 17/06/2016.
 */
public class DataProcessor {

    final String DIRECTORY_FLAG = "-o";

    private Lista<String> fileNameList = new Lista<>();
    private File directoryFile;

    private Lista<Diccionario<String, Integer>> dictionaryList = new Lista<>();
    private Grafica<String> graph = new Grafica<>();

    public DataProcessor(String[] args) throws IOException {
        getFilesAndDirectory(args);
        fillDictionaryList();
        doDictionaryGraph();
    }

    /**
     * Método privado el cual identifica los archivos y el directorio.
     *
     * @param args Los argumentos que se le fueron pasados en la consola.
     * @throws InvalidDirectoryException Si hubo algun error con el directorio.
     */
    private void getFilesAndDirectory(String[] args) throws InvalidDirectoryException {
        if (args.length == 0)
            throw new NoArgumentsGivenException();
        else
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(DIRECTORY_FLAG)) {
                    if (i + 1 >= args.length || !(new File(args[i + 1]).isDirectory()))
                        throw new InvalidDirectoryException();
                    directoryFile = new File(args[(i++) + 1]);
                } else {
                    fileNameList.agrega(args[i]);
                }
            }
    }

    /**
     * Método el cual llena los diccionarios.
     *
     * @throws IOException Una excepción con la entrada o salida.
     */
    private void fillDictionaryList() throws IOException {
        for (int i = 0; i < fileNameList.getElementos(); i++) {
            Diccionario<String, Integer> dictionary = new Diccionario<>();
            String str;
            BufferedReader br = new BufferedReader(new FileReader(fileNameList.get(i)));
            while ((str = br.readLine()) != null) {
                //Las separo por espacios.
                String[] words = str.split("\\s");
                for (int j = 0; j < words.length; j++) {
                    //Solamente dejo caracteres que son letras.
                    //Trato todas las palabras como si no tuvieran acentos.
                    String word = Normalizer
                        .normalize(words[j].replaceAll("\\P{L}+", "").toLowerCase(), Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    if (word.length() > 0)
                        if (dictionary.contiene(word))
                            dictionary.agrega(word, dictionary.get(word) + 1);
                        else
                            dictionary.agrega(word, 1);
                }
            }
            dictionaryList.agrega(dictionary);
        }
    }

    /**
     * Método auxiliar el cual dada una lista de {@link Diccionario} crea una grafica.
     */
    private void doDictionaryGraph() {
        Lista<String> aux = new Lista<>();
        fileNameList.forEach(fileName -> aux.agrega(new File(directoryFile, fileName).getName()));
        fileNameList = aux;
        fileNameList.forEach(graph::agrega);

        Lista<String> wordList = new Lista<>();
        int i = 0;
        int j = 0;

        for (Diccionario<String, Integer> dictionary_A : dictionaryList) {

            for (Diccionario<String, Integer> dictionary_B : dictionaryList) {
                //To reduce computation
                if (dictionary_A != dictionary_B) {
                    for (String key_A : dictionary_A.llaves())
                        if (key_A.length() >= 5 && dictionary_B.contiene(key_A))
                            if (!wordList.contiene(key_A))
                                wordList.agrega(key_A);
                }
                if (wordList.getLongitud() >= 10 && !graph.sonVecinos(fileNameList.get(i), fileNameList.get(j)))
                    graph.conecta(fileNameList.get(i), fileNameList.get(j));

                wordList.limpia();
                ++j;
            }//end for dictionary_A
            ++i;
            j = 0;
        }//end for dictionary_A
    }

    /**
     * Método que provee una lista con el nombre de los archivos.
     *
     * @return Una lista con el nombre de los archivos.
     */
    public Lista<String> getFileNameList() {
        return fileNameList;
    }

    /**
     * Método que provee el directorio identificado.
     *
     * @return El directorio identificado.
     */
    public File getDirectoryFile() {
        return directoryFile;
    }

    /**
     * Método que provee una lista de diccionarios.
     *
     * @return Una lista de diccionarios.
     */
    public Lista<Diccionario<String, Integer>> getDictionaryList() {
        return dictionaryList;
    }

    /**
     * Método que provee una gráfica hecha con los archivos.
     *
     * @return Una gráfica hecha con los archivos.
     */
    public Grafica<String> getGraph() {
        return graph;
    }
}
