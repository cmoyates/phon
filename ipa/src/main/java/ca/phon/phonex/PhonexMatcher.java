package ca.phon.phonex;

import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.FSAState.RunningState;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;

public class PhonexMatcher {
	
	/**
	 * Input
	 */
	private List<IPAElement> input;
	
	/**
	 * phonex Pattern
	 */
	private PhonexPattern pattern;
	
	/**
	 * Last fsa state
	 */
	private FSAState<IPAElement> lastMatchState = new FSAState<IPAElement>();
	
	/**
	 * Last match start index
	 */
	private int lastMatchStart = 0;
	
	/**
	 * Last append position
	 */
	private int lastAppendPosition = 0;
	
	/**
	 * Region
	 */
	private int regionStart = -1;
	
	private int regionEnd = -1;
	
	/**
	 * Constructor
	 */
	PhonexMatcher(PhonexPattern pattern, List<IPAElement> input) {
		this.pattern = pattern;
		this.input = input;
		reset();
	}
	
	/**
	 * Return this matcher's pattern.
	 * 
	 * @return the pattern for this matcher
	 */
	public PhonexPattern pattern() {
		return this.pattern;
	}
	
	/**
	 * Reset the matcher
	 */
	public void reset() {
		lastMatchState = new FSAState<IPAElement>();
		final int rs = (regionStart >= 0 ? regionStart : 0);
		final int re = (regionEnd >= 0 ? regionEnd : input.size());
		lastMatchState.setTape(input.subList(rs, re).toArray(new IPAElement[0]));
		lastMatchState.setTapeIndex(0);
		lastMatchStart = 0;
	}
	
	public void region(int regionStart, int regionEnd) {
		this.regionStart = regionStart;
		this.regionEnd = regionEnd;
		reset();
	}
	
	/**
	 * Reset the matcher with a new input sequence
	 * 
	 * @param input
	 */
	public void reset(List<IPAElement> input) {
		this.input = input;
		reset();
	}
	
	/**
	 * Reset matcher and perform a full input match test.
	 * 
	 * @param <code>true</code> if the entire input matches
	 *  the pattern, <code>false</code> otherwise
	 */
	public boolean matches() {
		reset();
		lastMatchState =
				pattern.getFsa().runWithTape(input.toArray(new IPAElement[0]), lastMatchState, true);
		return pattern.getFsa().isFinalState(lastMatchState.getCurrentState())
				&& lastMatchState.getRunningState() == RunningState.EndOfInput;
	}
	
	/**
	 * Reset matcher and attempt to find an occurance
	 * of the pattern in the given input.
	 * 
	 * Use {@link #group()}, {@link #start()}, and {@link #end()}
	 * for more information.
	 * 
	 * @param <code>true</code> if a sub-sequence of phones
	 *  is found that matches the pattern
	 */
	public boolean find() {
		boolean retVal = false;
		
		int currentIdx = lastMatchState.getTapeIndex();
		while(!retVal && currentIdx < input.size()) {
			
			lastMatchState.setTapeIndex(currentIdx);
			
			lastMatchState =
					pattern.getFsa().runWithTape(input.toArray(new IPAElement[0]), lastMatchState);
			retVal = 
					pattern.getFsa().isFinalState(lastMatchState.getCurrentState()) && (lastMatchState.getTapeIndex() != currentIdx);
			if(retVal)
				lastMatchStart = currentIdx;
			currentIdx++;
		}
		
		return retVal;
	}
	
	/**
	 * Reset matcher and attempt to find the next occurrence 
	 * of the pattern starting at the given index in the input.
	 * 
	 * @param index
	 * @return <code>true</code> if an occurrence of the pattern
	 *  was found, <code>false</code> otherwise
	 */
	public boolean find(int index) {
		reset();
		lastMatchState.setTapeIndex(index);
		return find();
	}
	
	/**
	 * Return the number of capturing groups (excluding
	 * group zero.)
	 * 
	 * @return the number of capturing groups
	 */
	public int groupCount() {
		return pattern.getFsa().getNumberOfGroups();
	}
	
	/**
	 * Return the start index of the last match.
	 * 
	 * @return the start index of the last match
	 *  (i.e., start index of group zero.)
	 */
	public int start() {
		return start(0);
	}
	
	/**
	 * Return the start index of the specified group.
	 * 
	 * @param gIdx
	 * @return the start index of the group <code>gIdx</code>,
	 *  or -1 if group data was not found
	 */
	public int start(int gIdx) {
		checkLastMatch();
		int retVal = 0;
		if(gIdx == 0) 
			retVal = lastMatchStart;
		else {
			retVal =
				(gIdx == 0 ? 0 : lastMatchState.getGroupStarts()[gIdx-1]);
		}
		return retVal;
	}
	
	/**
	 * Return the end index plus one of group zero
	 * 
	 * @return the end index plus one of group zero
	 */
	public int end() {
		return end(0);
	}
	
	/**
	 * Return the end index plus one of the specified
	 * group.
	 * 
	 * @param gIdx
	 * @return the end index of the specified group
	 *  plus one
	 */
	public int end(int gIdx) {
		checkLastMatch();
		int retVal = 0;
		if(gIdx == 0)
			retVal = lastMatchState.getTapeIndex();
		else {
			retVal =
					lastMatchState.getGroupStarts()[gIdx-1] +
					lastMatchState.getGroupLengths()[gIdx-1];
		}
		return retVal;
	}
	
	/**
	 * Check to make sure the match state is good.
	 * 
	 * @throws IllegalStateException
	 */
	private void checkLastMatch() 
		throws IllegalStateException {
		if(lastMatchState.getCurrentState() == null 
				|| !pattern.getFsa().isFinalState(lastMatchState.getCurrentState())) {
			throw new IllegalStateException("No previous match");
		}
	}
	
	/**
	 * Tells if the matcher has a current match.
	 * 
	 * @return <code>true</code> if the last call
	 *  to {@link #matches()} or {@link #find()}
	 *  was successful
	 */
	public boolean hasMatch() {
		boolean retVal = false;
		try {
			checkLastMatch();
			retVal = true;
		} catch (IllegalStateException e) {}
		return retVal;
	}
	
	/**
	 * <p>This method performs the following actions:
     * 
     * <ul>
     * <li>It reads {@link IPAElement}s from the input sequence, starting at the append position,
     * and appends them to the given {@link IPATranscriptBuilder}. It stops after reading the last 
	 * element preceding the previous match, that is, the element at index start() - 1.</li>
     * <li>It appends the given replacement string to the string buffer.</li>
     * <li>It sets the append position of this matcher to the index of the last element matched, 
     * plus one, that is, to end().</li>
     * </ul>
     * </p>
     * 
	 * <p>The replacement string may contain references to subsequences captured during the previous match:
	 * Each occurrence of ${name} or $g will be replaced by the result of evaluating the corresponding group(name) 
	 * or group(g) respectively. Only the numerals '0' through '9' are considered as potential components of the group reference.
     * If the second group matched the string "foo", for example, then passing the replacement string "$2bar" 
     * would cause "foobar" to be appended to the string buffer.</p>
	 * 
	 * @param builder
	 * @param replacement
	 */
	public void appendReplacement(IPATranscriptBuilder builder, IPATranscript replacement) {
		final ReplaceExpressionVisitor visitor = new ReplaceExpressionVisitor(this);
		replacement.accept(visitor);
		final IPATranscript replace = visitor.getTranscript();
		
		builder.append(input.subList(lastAppendPosition, start()));
		builder.append(replace);
		lastAppendPosition = end();
	}
	
	/**
	 * <p>Append {@link IPAElement}s from the input sequence to the {@link IPATranscriptBuilder}
	 * from the last append position to the end of the input sequence.</p>
	 * 
	 * @param builder
	 */
	public void appendTail(IPATranscriptBuilder builder) {
		builder.append(input.subList(lastAppendPosition, input.size()));
	}
	
	/**
	 * Return the group value for this match.  
	 * 
	 * @return group value for group zero
	 */
	public List<IPAElement> group() {
		return input.subList(start(), end());
	}
	
	/**
	 * Return the group value for the specified
	 * group
	 * 
	 * @param gIdx
	 * @return group value for the specified group
	 */
	public List<IPAElement> group(int gIdx) {
		return input.subList(start(gIdx), end(gIdx));
	}
	
}
