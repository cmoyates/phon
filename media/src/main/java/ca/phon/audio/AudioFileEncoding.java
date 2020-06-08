package ca.phon.audio;

/**
 * Audio file formats
 *
 */
public enum AudioFileEncoding {
	ALAW(1),
	MULAW(1),
	LINEAR_8_SIGNED(1),
	LINEAR_8_UNSIGNED(1),
	LINEAR_12_BIG_ENDIAN(2),
	LINEAR_12_LITTLE_ENDIAN(2),
	LINEAR_16_BIG_ENDIAN(2),
	LINEAR_16_LITTLE_ENDIAN(2),
	LINEAR_24_BIG_ENDIAN(3),
	LINEAR_24_LITTLE_ENDIAN(3),
	LINEAR_32_BIG_ENDIAN(4),
	LINEAR_32_LITTLE_ENDIAN(4),
	IEEE_FLOAT_32_BIG_ENDIAN(4),
	IEEE_FLOAT_32_LITTLE_ENDIAN(4),
	IEEE_FLOAT_64_BIG_ENDIAN(8),
	IEEE_FLOAT_64_LITTLE_ENDIAN(8),
	EXTENDED(0); // for other formats like mp3, flac, etc.
	
	private int bytesPerSample;
	
	private AudioFileEncoding(int bytesPerSample) {
		this.bytesPerSample = bytesPerSample;
	}
	
	public boolean isSigned() {
		return (this == LINEAR_8_SIGNED) || ordinal() > LINEAR_8_UNSIGNED.ordinal();
	}
	
	public boolean isBigEndian() {
		return (this == LINEAR_24_BIG_ENDIAN) ||
				(this == LINEAR_24_BIG_ENDIAN) ||
				(this == LINEAR_32_BIG_ENDIAN) ||
				(this == IEEE_FLOAT_32_BIG_ENDIAN) ||
				(this == IEEE_FLOAT_64_BIG_ENDIAN);
	}
	
	public int getBytesPerSample() {
		return bytesPerSample;
	}
	
}
