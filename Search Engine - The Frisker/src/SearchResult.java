/** Comparable class for Collection Results. */
public class SearchResult implements Comparable<SearchResult> {

	private String path;
	private Integer freq;
	private Integer first;

	/** Constructor for Comparable Objects. */
	public SearchResult(String path, Integer freq, Integer first) {
		this.path = path;
		this.freq = freq;
		this.first = first;
	}

	/** Updates the objects frequency and first occurence. */
	public void update(Integer freq, Integer first) {

		this.freq += freq;

		if(first < this.first) {
			this.first = first;
		}
	}

	@Override
	/** Overridden compareTo method that sorts comparables by freq, first, then path. */
	public int compareTo(SearchResult other) {

		if(!this.freq.equals(other.freq)) {
			return Integer.compare(other.freq, this.freq);
		}
		if(this.freq.equals(other.freq)) {

			if(!this.first.equals(other.first)) {
				return Integer.compare(this.first, other.first);
			}
			if(this.first.equals(other.first)) {

				if(!this.path.equals(other.path)) {
					return this.path.compareTo(other.path);
				}
				if(this.path.equals(other.path)) {
					return 0;
				}
			}
		}
		return 0;
	}

	/** compareTo method for comparing two integers. */
	public static int compareTo(Integer num1, Integer num2) {

		return Integer.compare(num1, num2);
	}

	@Override
	/** Overridden toString method for easily writing to output file. */
	public String toString() {

		return ('"' + this.path + '"' + ", " + this.freq + ", " + this.first);
	}
	
	public String getPath() {
		return path;
	}
}	