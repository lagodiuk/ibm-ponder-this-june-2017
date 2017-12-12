import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Solution of: https://www.research.ibm.com/haifa/ponderthis/challenges/June2017.html
 * @author Yurii Lahodiuk
 */
public class ArithmeticExpressionsSolver {

	public static void main(String[] args) throws Exception {

		RationalNumber target_value = new RationalNumber(100, 1);
		int max_digits = 7;
		int replacement_variants_count = 62;

		String solvable_numbers_file = "solvable.txt";
		String not_solvable_numbers_file = "non_solvable.txt";

		generate_all_solvable_numbers(target_value, max_digits, solvable_numbers_file);
		generate_all_not_solvable_numbers(max_digits, solvable_numbers_file, not_solvable_numbers_file);
		generate_result(solvable_numbers_file, not_solvable_numbers_file, max_digits, target_value, replacement_variants_count);
	}

	/**
	 * Finds all non-solvable numbers, which can be transformed into solvable numbers by replacing
	 * single digit
	 *
	 * @param solvable_numbers_file
	 * @param not_solvable_numbers_file
	 * @param max_digits
	 * @param target
	 * @param replacement_variants_count
	 */
	private static void generate_result(
			String solvable_numbers_file, String not_solvable_numbers_file, int max_digits, RationalNumber target, int replacement_variants_count)
			throws Exception {

		RationalNumber[][][] memoized_all_values = new RationalNumber[max_digits][POW_10[max_digits]][];

		int max_number = POW_10[max_digits];
		int[][] counter = new int[max_digits][max_number];

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(solvable_numbers_file), "UTF-8"))) {

			String s;
			StringBuilder sb = new StringBuilder();
			while ((s = br.readLine()) != null) {
				for (int del_position = 0; del_position < max_digits; del_position++) {
					sb.setLength(0);
					sb.append(s.trim());
					sb.deleteCharAt(del_position);
					int x = Integer.parseInt(sb.toString(), 10);
					counter[del_position][x]++;
				}
			}
		}

		char[] digits = "0123456789".toCharArray();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(not_solvable_numbers_file), "UTF-8"))) {

			String s;
			StringBuilder sb = new StringBuilder();
			while ((s = br.readLine()) != null) {
				int variants = 0;
				for (int del = 0; del < max_digits; del++) {
					sb.setLength(0);
					sb.append(s.trim());
					sb.deleteCharAt(del);
					int x = Integer.parseInt(sb.toString(), 10);
					variants += counter[del][x];
				}
				if (variants == replacement_variants_count) {
					boolean isPrime = is_prime(Integer.parseInt(s.trim()));
					String primality = isPrime ? "is_prime" : "is_not_prime";
					System.out.println(s + "\t" + primality);
					for (int del_position = 0; del_position < max_digits; del_position++) {
						for (char c : digits) {
							sb.setLength(0);
							sb.append(s.trim());
							sb.setCharAt(del_position, c);
							int x = Integer.parseInt(sb.toString(), 10);
							String reconstruction = construct_arithmetic_expression(x, max_digits, memoized_all_values, target);
							if (reconstruction != null) {
								System.out.println(sb.toString() + "\t" + reconstruction);
							}
						}
					}
					System.out.println();
				}
			}
		}
	}

	/**
	 * Simple primality checking routine
	 *
	 * @param n
	 * @return true in case when the number n is prime
	 */
	private static boolean is_prime(int n) {
		if (n % 2 == 0) {
			return false;
		}
		for (int i = 3; i * i <= n; i += 2) {
			if (n % i == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a file with all non-solvable numbers, with respect to the number of digits
	 * (the set of all non-solvable numbers is just a difference between the set of all numbers and
	 * the set of all solvable numbers)
	 *
	 * @param max_digits
	 * @param solvable_numbers_file
	 * @param not_solvable_numbers_file
	 */
	private static void generate_all_not_solvable_numbers(
			int max_digits, String solvable_numbers_file, String not_solvable_numbers_file) throws Exception {

		int max_number = POW_10[max_digits];
		boolean[] can_construct_target = new boolean[max_number];

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(solvable_numbers_file), "UTF-8"))) {
			String s;
			while ((s = br.readLine()) != null) {
				int x = Integer.parseInt(s, 10);
				can_construct_target[x] = true;
			}
		}

		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(not_solvable_numbers_file), "UTF-8"))) {

			for (int i = 0; i < can_construct_target.length; i++) {
				if (!can_construct_target[i]) {
					System.out.printf("%07d%n is not solvable", i);
					pw.printf("%07d%n", i);
				}
			}
		}
	}

	/**
	 * Creates a file with all solvable numbers, with respect to the target value and the number of
	 * digits
	 *
	 * @param target_value
	 * @param max_digits
	 * @param output_file_name
	 */
	private static void generate_all_solvable_numbers(RationalNumber target_value, int max_digits, String output_file_name) throws Exception {

		int max_number = POW_10[max_digits];
		Map<Key, Boolean> memoized = new HashMap<>(max_number);
		RationalNumber[][][] memoized_all_values = new RationalNumber[max_digits][POW_10[max_digits / 2 + 1]][];

		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(output_file_name), true), "UTF-8"))) {

			for (int i = 0; i < max_number; i++) {
				if (can_construct_target_value(i, max_digits, target_value, memoized, memoized_all_values)) {
					pw.printf("%07d%n", i);
				}
				if (i % 500 == 0) {
					// periodically clean-up the cache
					memoized.clear();
					pw.flush();
					System.out.println("Processed " + i);
				}
			}

			pw.flush();
		}
	}

	/**
	 * Check, whether the given number is solvable, with respect to the given target value
	 *
	 * @param x - tested number
	 * @param cnt - amount of digits
	 * @param target - target value of the arithmetic expression
	 * @param memoized - auxiliary memoization data structure
	 * @param memoized_all_values - auxiliary memoization data structure
	 */
	private static boolean can_construct_target_value(
			int x, int cnt, RationalNumber target, Map<Key, Boolean> memoized, RationalNumber[][][] memoized_all_values) {

		Key key = new Key(x, cnt, target);

		if (memoized.containsKey(key)) {
			return memoized.get(key);
		}

		if (cnt <= 4) {
			RationalNumber[] set = construct_all_possible_values(x, cnt, memoized_all_values);
			boolean contains = Arrays.binarySearch(set, target) >= 0;
			memoized.put(key, contains);
			return contains;
		}

		int rightCnt = 1;
		int leftCnt = cnt - rightCnt;

		for (int i = 0; i < cnt - 1; i++) {
			int mod = POW_10[rightCnt];
			int rightPart = x % mod;
			int leftPart = x / mod;
			if (rightPart < leftPart) {
				RationalNumber[] rightPartValues = construct_all_possible_values(rightPart, rightCnt, memoized_all_values);
				for (RationalNumber rightPartValue : rightPartValues) {
					for (Operation op : Operation.values()) {
						if (op.can_do_inverse_right(target, rightPartValue)) {
							RationalNumber leftPartValue = op.inverse_right(target, rightPartValue);
							if (can_construct_target_value(leftPart, leftCnt, leftPartValue, memoized, memoized_all_values)
									|| leftPartValue == RationalNumber.ANY) {
								memoized.put(key, true);
								return true;
							}
						}
					}
				}
			} else {
				RationalNumber[] leftPartValues = construct_all_possible_values(leftPart, leftCnt, memoized_all_values);
				for (RationalNumber leftPartValue : leftPartValues) {
					for (Operation op : Operation.values()) {
						if (op.can_do_inverse_left(target, leftPartValue)) {
							RationalNumber rightPartValue = op.inverse_left(target, leftPartValue);
							if (can_construct_target_value(rightPart, rightCnt, rightPartValue, memoized, memoized_all_values)
									|| rightPartValue == RationalNumber.ANY) {
								memoized.put(key, true);
								return true;
							}
						}
					}
				}
			}
			rightCnt++;
			leftCnt--;
		}

		memoized.put(key, false);
		return false;
	}

	/**
	 * Generates all values of all possible arithmetic expressions
	 *
	 * @param x - given number
	 * @param cnt - amount of digits
	 * @param memoized - auxiliary memoization data structure
	 * @return
	 */
	private static RationalNumber[] construct_all_possible_values(int x, int cnt, RationalNumber[][][] memoized) {

		if (cnt == 1) {
			if (x > 0) {
				return new RationalNumber[]{new RationalNumber(-x, 1), new RationalNumber(x, 1)};
			}
			else {
				return new RationalNumber[]{new RationalNumber(x, 1)};
			}
		}

		if (memoized[cnt][x] != null) {
			return memoized[cnt][x];
		}

		Set<RationalNumber> possible_values = new HashSet<>();
		for (int i = 1; i < cnt; i++) {
			int mod = pow10(i);
			int x1 = x % mod;
			int x2 = x / mod;
			RationalNumber[] res1 = construct_all_possible_values(x2, cnt - i, memoized);
			RationalNumber[] res2 = construct_all_possible_values(x1, i, memoized);
			for (RationalNumber r1 : res1) {
				for (RationalNumber r2 : res2) {
					for (Operation op : Operation.values()) {
						if (op == Operation.DIV && r2.isZero()) {
							continue;
						}
						possible_values.add(op.apply(r1, r2));
					}
				}
			}
		}

		RationalNumber[] result = possible_values.toArray(new RationalNumber[0]);
		Arrays.sort(result);
		memoized[cnt][x] = result;
		return result;
	}

	/**
	 * Constructs an arithmetic expression, which evaluates to the given target value
	 *
	 * @param x - given number
	 * @param cnt - amount of digits
	 * @param memoized - auxiliary memoization data structure
	 * @param target - target value of the arithmetic expression
	 */
	private static String construct_arithmetic_expression(int x, int cnt, RationalNumber[][][] memoized, RationalNumber target) {
		if (cnt == 1) {
			return target.toString();
		}

		for (int i = 1; i < cnt; i++) {
			int mod = pow10(i);
			int x1 = x % mod;
			int x2 = x / mod;
			RationalNumber[] res1 = construct_all_possible_values(x2, cnt - i, memoized);
			RationalNumber[] res2 = construct_all_possible_values(x1, i, memoized);
			for (RationalNumber r1 : res1) {
				for (RationalNumber r2 : res2) {
					for (Operation op : Operation.values()) {
						if (op == Operation.DIV && r2.isZero()) {
							continue;
						}
						if (op.apply(r1, r2).equals(target)) {
							return "(" + construct_arithmetic_expression(x2, cnt - i, memoized, r1) + ")" + op.toString() + "("
									+ construct_arithmetic_expression(x1, i, memoized, r2) + ")";
						}
					}
				}
			}
		}

		return null;
	}

	// Just a container to the powers of 10
	private static int[] POW_10 = new int[10];
	static {
		for (int i = 0; i < POW_10.length; i++) {
			POW_10[i] = pow10(i);
		}
	}
	private static int pow10(int p) {
		int x = 1;
		for (int i = 0; i < p; i++) {
			x *= 10;
		}
		return x;
	}

	/**
	 * Binary operations
	 */
	enum Operation {
		ADD {
			@Override
			RationalNumber apply(RationalNumber n1, RationalNumber n2) {
				return n1.add(n2);
			}

			@Override
			public String toString() {
				return "+";
			}

			@Override
			RationalNumber inverse_left(RationalNumber target, RationalNumber left) {
				return target.subtract(left);
			}

			@Override
			RationalNumber inverse_right(RationalNumber target, RationalNumber right) {
				return target.subtract(right);
			}

			@Override
			boolean can_do_inverse_left(RationalNumber target, RationalNumber left) {
				return true;
			}

			@Override
			boolean can_do_inverse_right(RationalNumber target, RationalNumber right) {
				return true;
			}
		},
		SUB {
			@Override
			RationalNumber apply(RationalNumber n1, RationalNumber n2) {
				return n1.subtract(n2);
			}

			@Override
			public String toString() {
				return "-";
			}

			@Override
			RationalNumber inverse_left(RationalNumber target, RationalNumber left) {
				return left.subtract(target);
			}

			@Override
			RationalNumber inverse_right(RationalNumber target, RationalNumber right) {
				return target.add(right);
			}

			@Override
			boolean can_do_inverse_left(RationalNumber target, RationalNumber left) {
				return true;
			}

			@Override
			boolean can_do_inverse_right(RationalNumber target, RationalNumber right) {
				return true;
			}
		},
		MUL {
			@Override
			RationalNumber apply(RationalNumber n1, RationalNumber n2) {
				return n1.multiply(n2);
			}

			@Override
			public String toString() {
				return "*";
			}

			@Override
			RationalNumber inverse_left(RationalNumber target, RationalNumber left) {
				if (left.isZero() && target.isZero()) {
					return RationalNumber.ANY;
				}
				return target.divide(left);
			}

			@Override
			RationalNumber inverse_right(RationalNumber target, RationalNumber right) {
				if (right.isZero() && target.isZero()) {
					return RationalNumber.ANY;
				}
				return target.divide(right);
			}

			@Override
			boolean can_do_inverse_left(RationalNumber target, RationalNumber left) {
				if (left.isZero() && target.isZero()) {
					return true;
				}
				return !(left.isZero() || target.isZero());
			}

			@Override
			boolean can_do_inverse_right(RationalNumber target, RationalNumber right) {
				if (right.isZero() && target.isZero()) {
					return true;
				}
				return !(right.isZero() || target.isZero());
			}
		},
		DIV {
			@Override
			RationalNumber apply(RationalNumber n1, RationalNumber n2) {
				return n1.divide(n2);
			}

			@Override
			public String toString() {
				return "/";
			}

			@Override
			RationalNumber inverse_left(RationalNumber target, RationalNumber left) {
				if (left.isZero() && target.isZero()) {
					return RationalNumber.ANY;
				}
				return left.divide(target);
			}

			@Override
			RationalNumber inverse_right(RationalNumber target, RationalNumber right) {
				return target.multiply(right);
			}

			@Override
			boolean can_do_inverse_left(RationalNumber target, RationalNumber left) {
				if (left.isZero() && target.isZero()) {
					return true;
				}
				return !(left.isZero() || target.isZero());
			}

			@Override
			boolean can_do_inverse_right(RationalNumber target, RationalNumber right) {
				return !right.isZero();
			}
		};

		abstract RationalNumber apply(RationalNumber left, RationalNumber right);
		abstract RationalNumber inverse_left(RationalNumber target, RationalNumber left);
		abstract RationalNumber inverse_right(RationalNumber target, RationalNumber right);
		abstract boolean can_do_inverse_left(RationalNumber target, RationalNumber left);
		abstract boolean can_do_inverse_right(RationalNumber target, RationalNumber right);

		@Override
		public String toString() {
			return super.toString();
		}
	}

	/**
	 * Rational numbers handling
	 */
	private static final class RationalNumber implements Comparable<RationalNumber> {

		public static final RationalNumber ANY = new RationalNumber(Integer.MAX_VALUE, 1);

		private final int numerator;

		private final int denominator;

		public RationalNumber(int numer, int denom) {
			if (denom == 0) {
				throw new ArithmeticException();
			}

			// Numerator stores the sign
			if (denom < 0) {
				numer = -numer;
				denom = -denom;
			}

			if (numer != 0) {
				int gcd = this.gcd(Math.abs(numer), denom);
				numer /= gcd;
				denom /= gcd;
			}

			this.numerator = numer;
			this.denominator = denom;
		}

		public final RationalNumber add(RationalNumber other) {
			int commonDenominator = this.denominator * other.denominator;
			int numerator1 = this.numerator * other.denominator;
			int numerator2 = other.numerator * this.denominator;
			int sum = numerator1 + numerator2;
			return new RationalNumber(sum, commonDenominator);
		}

		public final RationalNumber subtract(RationalNumber other) {
			int commonDenominator = this.denominator * other.denominator;
			int numerator1 = this.numerator * other.denominator;
			int numerator2 = other.numerator * this.denominator;
			int difference = numerator1 - numerator2;
			return new RationalNumber(difference, commonDenominator);
		}

		public final RationalNumber multiply(RationalNumber other) {
			int numer = this.numerator * other.numerator;
			int denom = this.denominator * other.denominator;
			return new RationalNumber(numer, denom);
		}

		public final RationalNumber divide(RationalNumber other) {
			if (other.numerator == 0) {
				throw new ArithmeticException();
			}
			int numer = this.numerator * other.denominator;
			int denom = this.denominator * other.numerator;
			return new RationalNumber(numer, denom);
		}

		@Override
		public String toString() {
			String result;
			if (this.numerator == 0) {
				result = "0";
			} else if (this.denominator == 1) {
				result = Integer.toString(this.numerator);
			} else {
				result = this.numerator + "/" + this.denominator;
			}
			return result;
		}

		public final boolean isZero() {
			return this.numerator == 0;
		}

		private final int gcd(int a, int b) {
			int tmp;
			while (a != 0 && b != 0) {
				tmp = b;
				b = a % b;
				a = tmp;
			}
			return a + b;
		}

		@Override
		public final boolean equals(Object other) {
			RationalNumber that = (RationalNumber) other;
			if (this.numerator != that.numerator) {
				return false;
			}
			if (this.numerator == 0) {
				return true;
			}
			return this.denominator == that.denominator;

		}

		@Override
		public final int hashCode() {
			int result = this.numerator;
			if (this.numerator != 0) {
				result = 31 * result + this.denominator;
			}
			return result;
		}

		@Override
		public final int compareTo(RationalNumber other) {
			return Integer.compare(this.numerator * other.denominator, other.numerator * this.denominator);
		}
	}

	private static class Key {
		private final int x;
		private final int cnt;
		private final RationalNumber rn;
		public Key(int x, int cnt, RationalNumber rn) {
			this.x = x;
			this.cnt = cnt;
			this.rn = rn;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.cnt;
			result = prime * result + this.rn.hashCode();
			result = prime * result + this.x;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			Key other = (Key) obj;
			if (this.cnt != other.cnt) {
				return false;
			}
			if (this.x != other.x) {
				return false;
			}
			if (!this.rn.equals(other.rn)) {
				return false;
			}
			return true;
		}
	}
}
