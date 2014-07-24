import java.awt.List;
import java.util.ArrayList;
import java.util.Set;

import edu.neumont.nlp.DecodingDictionary;

public class ExhaustiveDecoder {
	private DecodingDictionary dic;
	private int freq;
	private int branchCount;
	
	public ExhaustiveDecoder(DecodingDictionary dd, int freq)
	{
		dic = dd;
		this.freq = freq;
		branchCount = 0;
	}
	
	public ArrayList<ChainFreq> decode(String message)
	{
		long start = System.currentTimeMillis();
		ArrayList<ChainFreq> Successes = new ArrayList<ChainFreq>();
		ArrayList<MorseWords> words = obtainPossibilities(message, 0);
		for(int i=0; i<words.size(); i++)
		{
			ArrayList<MorseWords> nextWord = obtainPossibilities(message.substring(words.get(i).length, message.length()), words.get(i).length);
			ArrayList<MorseWords> soFar = new ArrayList<MorseWords>();
			soFar.add(words.get(i));
			decodeHelper(soFar, nextWord, Successes, message, 1.0f);
		}
		sortList(Successes);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println(message.length());
		System.out.println(branchCount);
		return Successes;
	}
	
	private boolean decodeHelper(ArrayList<MorseWords> SoFar, ArrayList<MorseWords> ToGo, ArrayList<ChainFreq> Successes, String message, double totalFreq)
	{
		if((SoFar.size() > 1 && dic.frequencyOfFollowingWord(SoFar.get(SoFar.size()-2).word, SoFar.get(SoFar.size()-1).word) < freq) || totalFreq < .00000008 )
		{
			branchCount++;
			return false;
		}
		
		if(ToGo.size() <= 0)
		{
			branchCount++;
			MorseWords mw = new MorseWords("",0,null);
			for(int i=0; i<SoFar.size();i++)
			{
				mw.word += SoFar.get(i).word;
			}
			ChainFreq toAdd = new ChainFreq(mw.word, totalFreq);
			Successes.add(toAdd);
			return true;
		}
		
		
		for(int i=0; i<ToGo.size(); i++)
		{
			double nextFreq = totalFreq;
			if(SoFar.size() > 1)
			{
				nextFreq *= (double)dic.frequencyOfFollowingWord(SoFar.get(SoFar.size()-2).word, SoFar.get(SoFar.size()-1).word)/(double)10000;
			}
			MorseWords value = ToGo.get(i);
			ArrayList<MorseWords> newSoFar = new ArrayList(SoFar);
			newSoFar.add(value);
			ArrayList<MorseWords> newToGo = obtainPossibilities(message.substring(value.length, message.length()), value.length);
			decodeHelper(newSoFar, newToGo, Successes, message, nextFreq);
		}
		return true;
	}
	
	private ArrayList<MorseWords> obtainPossibilities(String message, int previousLength)
	{
		ArrayList<MorseWords> words = new ArrayList<MorseWords>();
		for(int i=0; i<message.length();)
		{
			Set<String> possibilities = dic.getWordsForCode(message);
			if(possibilities !=  null)
			{
				for(String s: possibilities)
				{
					words.add(new MorseWords(s, message.length()+previousLength, message));
				}
			}
			if(message.length()>1)
			{
				message = message.substring(0, message.length()-1);
			}
			else
			{
				message = "";
			}
		}
		return words;
	}

	private void sortList(ArrayList<ChainFreq> cf)
	{
		for(int i=0; i<cf.size(); i++)
		{
			for(int j=i+1; j<cf.size(); j++)
			{
				if(cf.get(i).freq < cf.get(j).freq)
				{
					ChainFreq temp = cf.get(i);
					cf.set(i, cf.get(j));
					cf.set(j, temp);
				}
			}
		}
		ArrayList<ChainFreq> temp = new ArrayList<ChainFreq>();
		java.util.List<ChainFreq> list = (cf.size() > 20)? cf.subList(0, 20): cf.subList(0, cf.size());
		temp.addAll(list);
	}
}

