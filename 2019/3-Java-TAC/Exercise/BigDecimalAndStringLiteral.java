import java.math.BigDecimal;

class BigDecimalAndStringLiteral {

	public strictfp static void main(String[] args) {
		System.out.println(new BigDecimal(0.1f)); // NOT OK
		System.out.println(new BigDecimal(0.1d)); // NOT OK

		float f = (args.length + 1.0f / 3.0f);
		System.out.println(new BigDecimal(f)); // OK
		System.out.println(new BigDecimal("0.1")); // OK
	}
}
