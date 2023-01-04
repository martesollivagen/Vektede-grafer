import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class VektedeGrafer {

    public static void main(String[] args) {
        Graph graf = new Graph();
        graf.nyUgraf( "src/vg5");
        System.out.println(graf.printDijkstra(1));
    }
}

class Graph {
    int N, K;
    Node[] node;
    Node dummy;

    int over(int i){return (i-1)>>1;}
    int venstre(int i){return (i<<1)+1;}
    int høyre(int i){return (i+1)<<1;}

    public void nyUgraf(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))){
            StringTokenizer st = new StringTokenizer(br.readLine());
            N = Integer.parseInt(st.nextToken());
            node = new Node[N];
            for (int i = 0; i<N; i++){
                node[i] = new Node();
                node[i].index = i;
            }
            K = Integer.parseInt(st.nextToken());
            for (int i = 0; i<K; i++){
                st = new StringTokenizer(br.readLine());
                int fra = Integer.parseInt(st.nextToken());
                int til = Integer.parseInt(st.nextToken());
                int vekt = Integer.parseInt(st.nextToken());
                Vkant k = new Vkant(node[til], (Vkant) node[fra].kant1, vekt);
                node[fra].antallKanter++;
                node[fra].kant1 = k;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void forkort(Node n, Vkant k){
        Forgj nd = (Forgj) n.d, md = (Forgj) k.til.d;
        if (md.distanse > nd.distanse + k.vekt){
            md.distanse = nd.distanse + k.vekt;
            md.forgj = n;
        }
    }

    public void setDummy(){
        Node dummy = new Node();
        dummy.index = -1;
        dummy.d = new Forgj();
        this.dummy = dummy;
    }

    public void fiks_heap(int i, Node[] list){ // se på?
        int m = venstre(i);

        if (m < list.length){
            int h = m + 1;
            if (h < list.length && (((Forgj) list[h].d).finn_dist() < ((Forgj) list[m].d).finn_dist())){
                m = h;
            }
            if (((Forgj) list[m].d).finn_dist() < ((Forgj) list[i].d).finn_dist()){
                bytt(list,i,m);
                fiks_heap(m, list);
            }
        }
    }

    public void lagPriKo(Node[] list){
        if (N >= 0) System.arraycopy(node, 0, list, 0, N);
        lag_heap(list);
    }

    public void lag_heap(Node[] list){
        int i = list.length/2;
        while (i-->0) fiks_heap(i, list);
    }



    public void dijkstra(Node s){
        initforgj(s);
        setDummy();
        Node[] pri = new Node[N];
        lagPriKo(pri);
        for (int i = N; i > 1; --i){
            Node n = hent_min(i, pri);

            for (Vkant k = n.kant1; k != null; k = (Vkant) k.neste){
                forkort(n,k);
            }
        }
    }

    public static void bytt(Node[]t, int i, int j){
        Node k = t[j];
        t[j]=t[i];
        t[i]=k;
    }

    public Node hent_min(int i, Node[] list){
        lag_heap(list);
        Node min = list[0];
        list[0] = list[i-1];
        list[i-1] = dummy;
        fiks_heap(0, list);
        return min;
    }

    public String printDijkstra(int s){
        dijkstra(node[s]);
        StringBuilder sb = new StringBuilder("Node   Forgjenger   Distanse");
        for (Node node2 : node) {
            if (node2 == node[s]){
                sb.append("\n").append(node2.index).append(" ".repeat(11)).append("start").append(" ".repeat(10)).append(((Forgj) (node2.d)).distanse);
            }
            else if (((Forgj) (node2.d)).finnForgj() != null){
                sb.append("\n").append(node2.index).append(" ".repeat(15)).append(((Forgj) (node2.d)).finnForgj().index).append(" ".repeat(10)).append(((Forgj) (node2.d)).distanse);
            }
            else {
                sb.append("\n").append(node2.index).append(" ".repeat(18)).append("Nåes ikke");
            }
        }
        return sb.toString();
    }

    public void initforgj(Node s){
        for(int i = N; i-->0;){
            node[i].d = new Forgj();
        }
        ((Forgj)s.d).distanse = 0;
    }

    public void dfs_init(){
        for (int i = N; i-->0;){
            node[i].d = new Dfs_forgj();
        }
        Dfs_forgj.null_tid();
    }

    public void df_sok(Node n){
        Dfs_forgj nd = (Dfs_forgj) n.d;
        nd.funnet_tid = Dfs_forgj.les_tid();
        for(Kant k = n.kant1; k!=null; k=k.neste){
            Dfs_forgj md = (Dfs_forgj) k.til.d;
            if (md.funnet_tid == 0){
                md.forgj = n;
                md.distanse = nd.distanse +1;
                df_sok(k.til);
            }
        }
        nd.ferdig_tid = Dfs_forgj.les_tid();
        n.setFerdig_tid(nd.ferdig_tid);
    }

    public void dfs(Node s){
        dfs_init();
        ((Dfs_forgj)s.d).distanse = 0;
        df_sok(s);
    }

    public void setFerdigTid(){
        dfs(node[0]);
        for(Node n: node){
            if (n.ferdig_tid == 0){
                df_sok(n);
            }
        }
    }

    private int fintIndex(Node n){
        if (n == null) return -1;
        return List.of(node).indexOf(n);
    }
}

class Kant {
    Kant neste;
    Node til;

    public Kant(Node n, Kant neste){
        til = n;
        this.neste = neste;
    }
}

class Vkant extends Kant {
    int vekt;

    public Vkant(Node n, Vkant k, int vekt){
        super(n,k);
        this.vekt = vekt;
    }
}

class Node {
    Vkant kant1;
    int antallKanter;
    int ferdig_tid;
    int index;
    int minstAvstandDik;
    Object d;

    public Node(){
        kant1 = null;
    }

    public int getD(){
        Forgj fj = (Forgj)d;
        return fj.finn_dist();
    }

    public int getFerdig_tid() {
        return ferdig_tid;
    }

    public void setFerdig_tid(int ferdig_tid) {
        this.ferdig_tid = ferdig_tid;
    }

    public void setMinstAvstandDik(int minstAvstandDik){
        this.minstAvstandDik = minstAvstandDik;
    }
}

class Forgj {
    int distanse;
    Node forgj;
    static int uendelig = 1000000000;

    public Forgj(){
        distanse = uendelig;
    }

    public int finn_dist(){
        return distanse;
    }

    public Node finnForgj(){
        return forgj;
    }
}

class Dfs_forgj extends Forgj {
    int funnet_tid, ferdig_tid;
    static int tid;

    static void null_tid(){
        tid = 0;
    }

    static int les_tid(){
        return ++tid;
    }

    public int getFunnet(){
        return funnet_tid;
    }
}