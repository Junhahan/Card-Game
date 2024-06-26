import java.util.ArrayList;

class card{
    int value;
    String fileName;

    public card(int v, String f){
        this.value = v;
        this.fileName = f;
    }

    /// get the value of the card for comparison
    public int getValue(){
        return value;
    }

    /// get the file name of the card for presenting in the GUI
    public String getFileName(){
        return fileName;
    }
}

public class Deck{
    private ArrayList<card> cards;

    /// constructor for deck, each card holds file name composed of suit and value in a string as well as just the value
    public Deck(){
        int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        String[] suits = {"clubs", "diamonds", "hearts", "spades"};
        String[] nameValues = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace"};

        // define the cards in the array list
        cards = new ArrayList<>();

        for(int v = 0; v<values.length; v++){
            for(String s : suits){
                String fileName = "/Documents/cards/" + nameValues[v] + "_of_" + s + ".png";
                cards.add(new card(values[v], fileName));
            }
        }
    }

    /// shuffle deck
    public void shuffle() {
        int l = cards.size();
        for(int x = 0; x < l; x++){
            // round random to the nearest int 
            int randIndex = x + (int)(Math.random() * (l - x));
            //simple swap
            card temp = cards.get(randIndex);
            cards.set(randIndex, cards.get(x));
            cards.set(x, temp);
        }    
    }

    /// give hand (in every case) 31 cards or half the deck
    public ArrayList<card> dealHand(){
        ArrayList<card> hand = new ArrayList<>();
        for (int i = 0; i < 25; i++) hand.add(cards.remove(0));

        return hand;
    }
}