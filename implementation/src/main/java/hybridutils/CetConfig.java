package hybridutils;

import java.nio.file.Paths;
import java.util.ArrayList;

public class CetConfig {
    private final String type;
    private final String inputFile;
    private final int windowl;
    private final int slidel;
    private final int parallelism;
    private final boolean pdfs;
    private final boolean write;
    private final String resultsFile;
    private final int prednum;
    private final String[] predlist;
    private final boolean isSimple;

    private CetConfig(CetConfig.Builder builder) {
        this.type = builder.type;
        this.inputFile = builder.inputFile;
        this.windowl = builder.windowl;
        this.slidel = builder.slidel;
        this.parallelism = builder.parallelism;
        this.pdfs = builder.pdfs;
        this.write = builder.write;
        this.resultsFile = builder.resultsFile;
        this.prednum = builder.prednum;
        this.predlist = builder.predlist;
        this.isSimple = builder.isSimple;
    }

    public String[] getConfig(){

        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(type);
        arguments.add("-i");
        arguments.add(inputFile);
        arguments.add("-wl");
        arguments.add(String.valueOf(windowl));
        arguments.add("-sl");
        arguments.add(String.valueOf(slidel));
        arguments.add("-p");
        arguments.add(String.valueOf(parallelism));
        if(isSimple && type.equals("stock"))
            arguments.add("-simple");
        if(pdfs)
            arguments.add("-pdfs");
        if(write){
            arguments.add("-w");
            arguments.add(Paths.get(System.getProperty("user.dir")).getParent()+"/results-test");
            arguments.add("-out");
        }
        if(prednum>0){
            arguments.add("-prednum");
            arguments.add(String.valueOf(prednum));
            for(int i=0;i<prednum;i++)
                arguments.add(predlist[i]);
        }

        String[] args = new String[arguments.size()];
        for(int i=0;i<args.length;i++)
            args[i] = arguments.get(i);

        return args;
    }

    public static class Builder {
        private String type = "stock";
        private String inputFile = "test.stream";
        private int windowl = 100;
        private int slidel = 1;
        private int parallelism = 4;
        private boolean pdfs = true;
        private boolean write = true;
        private String resultsFile = "results-test";
        private int prednum = 0;
        private String[] predlist = new String[0];
        private boolean isSimple = true;

        public Builder type(String type){
            this.type = type;
            return this;
        }

        public Builder inputFile(String inputFile) {
            this.inputFile = inputFile;
            return this;
        }

        public Builder windowLength(int wl) {
            this.windowl = wl;
            return this;
        }

        public Builder slideLength(int sl) {
            this.slidel = sl;
            return this;
        }

        public Builder parallelism(int p) {
            this.parallelism = p;
            return this;
        }

        public Builder pdfs(boolean pdfs) {
            this.pdfs = pdfs;
            return this;
        }

        public Builder outFile(String resultsFile) {
            this.resultsFile = resultsFile;
            this.write = true;
            return this;
        }

        public Builder preds(String[] predlist){
            this.predlist = predlist;
            this.prednum = predlist.length;
            return this;
        }

        public Builder isSimple(boolean isSimple){
            this.isSimple = isSimple;
            return this;
        }


        public CetConfig build() {
            return new CetConfig(this);
        }
    }

}
