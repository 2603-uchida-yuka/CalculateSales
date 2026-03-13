package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_ITEM_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_ITEM_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String BRANCH_FILE = "支店定義ファイル";
	private static final String ITEM_FILE = "商品定義ファイル";
	private static final String FILE_NOT_EXIST = "が存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String FILE_NOT_SERIAL_NAME = "売上ファイル名が連番になっていません";
	private static final String MAX_SALES_AMOUNT = "合計⾦額が10桁を超えました";
	private static final String CODE_NOT_EXIST = "が不正です";
	private static final String BRANCH_CODE = "の支店コード";
	private static final String ITEM_CODE = "の商品コード";
	private static final String SALESFILE_INVALID_FORMAT = "のフォーマットが不正です";
	// 正規表現パターン
	private static final String REGREX = "^[0-9]{8}\\.rcd$";
	private static final String CORE_REGREX = "^\\d{3}$";
	private static final String ITEM_REGREX = "^[a-zA-Z0-9]{8}$";
	private static final String NUMBER_REGREX = "\\d+";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
			//エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理→指摘1:正規表現渡す
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, BRANCH_FILE, CORE_REGREX)) {
			return;
		}

		// 商品コードと商品名を保持するMap
		Map<String, String> itemNames = new HashMap<>();
		// 商品コードと商品金額を保持するMap
		Map<String, Long> itemSales = new HashMap<>();

		// 商品定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_ITEM_LST, itemNames, itemSales, ITEM_FILE, ITEM_REGREX)) {
			return;
		}
		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//listFilesを使用してfilesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣⾔します。
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for (int i = 0; i < files.length; i++) {
			// エラー処理3
			//matches を使⽤してファイル名が「数字8桁.rcd」なのか判定します。
			if (files[i].isFile() && files[i].getName().matches(REGREX)) {
				rcdFiles.add(files[i]);
			}

		}

		// エラー処理2-1
		// 売上ファイルを保持しているListをソートする
		Collections.sort(rcdFiles);
		//⽐較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
			if ((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println(FILE_NOT_SERIAL_NAME);
				return;
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			//trueの場合の処理
			BufferedReader br = null;

			try {
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				List<String> fileContents = new ArrayList<>();

				while ((line = br.readLine()) != null) {
					fileContents.add(line);
				}

				// エラー処理2
				if (fileContents.size() != 3) {
					//売上ファイルの⾏数が3⾏ではなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(rcdFiles.get(i).getName() + SALESFILE_INVALID_FORMAT);
					return;
				}

				// エラー処理2
				// 支店コード
				String branchCd = fileContents.get(0);
				if (!branchNames.containsKey(branchCd)) {
					//⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(rcdFiles.get(i).getName() + BRANCH_CODE + CODE_NOT_EXIST);
					return;
				}

				// 商品コード→指摘2:存在チェックが抜けてる
				String itemCd = fileContents.get(1);
				if (!itemNames.containsKey(itemCd)) {
					//⽀店情報を保持しているMapに売上ファイルの商品コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(rcdFiles.get(i).getName() + ITEM_CODE + CODE_NOT_EXIST);
					return;
				}

				// 金額
				String amount = fileContents.get(2);
				if (!amount.matches(NUMBER_REGREX)) {
					//売上⾦額が数字ではなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				// 集計ファイルにそれぞれ金額を集計する
				Long saleAmount = branchSales.get(branchCd) + Long.parseLong(amount);
				Long itemAmount = itemSales.get(itemCd) + Long.parseLong(amount);

				if (saleAmount >= 10000000000L || itemAmount >= 10000000000L) {//指摘3
					//売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表⽰します。
					System.out.println(MAX_SALES_AMOUNT);
					return;
				}

				//加算した売上⾦額をMapに追加します。
				branchSales.put(branchCd, saleAmount);
				itemSales.put(itemCd, itemAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_ITEM_OUT, itemNames, itemSales)) {
			return;
		}

	}

	/**
	 * 支店・商品定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店・商品コードと支店・商品名を保持するMap
	 * @param 支店・商品コードと売上金額を保持するMap
	 * @param エラーファイル名
	 * @param 正規表現
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales, String errorFileName, String regrex) {//指摘4:引数に定数を渡す
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			// エラー処理1
			if (!file.exists()) {
				//定義ファイルが存在しない場合、コンソールにエラーメッセージを表⽰します。

				System.out.println(errorFileName + FILE_NOT_EXIST);

				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//split を使って「,」(カンマ)で分割すると、
				//items[0] には支店コード、items[1] には支店名が格納されます。
				String[] items = line.split(",");

				// エラー処理1
				if (items.length != 2 || !items[0].matches(regrex)) {
					//支店定義ファイルの仕様が満たされていない場合、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(errorFileName + FILE_INVALID_FORMAT);
					return false;
				}

				//Mapに追加する2つの情報を putの引数として指定します。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店・商品別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店・商品コードと名を保持するMap
	 * @param 支店・商品コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		// 3. 便利な書き込みメソッド（printlnなど）を使えるようにする
		BufferedWriter bw = null;

		try {
			// 書き込み処理
			// 例外が起きる可能性がある処理をすべてtryで囲む
			FileWriter fw = new FileWriter(path + "\\" + fileName);
			bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			// コードの集合でループを回す
			for (String code : branchNames.keySet()) {
				// ファイルへ書き込み
				pw.println(code + "," + branchNames.get(code) + "," + branchSales.get(code));
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
