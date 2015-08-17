package com.esen.study.xml.clover;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esen.util.FileFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

/**
 * clover覆盖率的报告，如：irpt的，还包含了util和jdbc的内容
 * 现提取指定包路径下的覆盖率，算出覆盖率结果
 * 
 * clover.xml大概的格式：<br/>
 * &lt;package name="packagename"><br/>
 * 	&lt;metrics coveredelements="" statements="" methods="" elements="" files="" ... /><br/>
 * 		&lt;file path="" name=""><br/>
 * 			&lt;metrics cloverelements="" statements="" methods="" classes="" elements="" ... /><br/>
 * 				&lt;class name=""><br/>
 * 					&lt;metrics cloverelements="" statements="" methods="" elements="" ... /><br/>
 * 				&lt;/class><br/>
 * 		&lt;/file><br/>
 * &lt;/package><br/>
 * 
 * 计算方式：<br/>
 * cloverelements/elements
 * 
 * @author chenlan
 */
public class TestReadCloverXml {
	/**
	 * clover报告的工程路径名称
	 */
	private static final String RESPONSEPATH = "E:\\gitcode\\gitrepertory\\";

	/**
	 * project节点
	 */
	private static final String PROJECT = "project";

	/**
	 * metrics节点
	 */
	private static final String METRICS = "metrics";

	/**
	 * file节点
	 */
	private static final String FILE = "file";

	/**
	 * class节点
	 */
	private static final String CLASS = "class";

	/**
	 * package节点
	 */
	private static final String PACKAGE = "package";

	/**
	 * coveredelements节点
	 */
	private static final String COVEREDELEMENTS = "coveredelements";

	/**
	 * statements节点
	 */
	private static final String STATEMENTS = "statements";

	/**
	 * methods节点
	 */
	private static final String METHODS = "methods";

	/**
	 * elements节点
	 */
	private static final String ELEMENTS = "elements";

	/**
	 * files属性
	 */
	private static final String FILES = "files";

	/**
	 * classes属性
	 */
	private static final String CLASSES = "classes";

	/**
	 * path属性
	 */
	private static final String PATH = "path";

	private static List<CloverBean> list = new ArrayList<CloverBean>();

	private static int javacount = 0;

	private static final String RESULTPATH = "C:/Users/Administrator/Desktop/clover.csv";

	/**
	 * 根据文件路径来判断是否是工程下的
	 */
	@Test
	public void testGetCloverByPath() throws Exception {
		File file = new File(RESULTPATH);
		FileOutputStream fos = new FileOutputStream(file);
		try {
			// csv的UTF-8格式需要BOM文件头，否则打开会乱码
			fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		} finally {
			fos.close();
		}
		double clover = TestReadCloverXml.getCloverByPath("E:/clover/irpt/clover/irptweb-server-20150803/clover.xml",
				"irpt");
		System.out.println(clover);
	}

	/**
	 * @param xmlpath clover.xml 的路径
	 * @param project 工程名称
	 * @return 
	 * @throws Exception 
	 */
	private static double getCloverByPath(String xmlpath, String project) throws Exception {
		File file = new File(xmlpath);
		FileInputStream fis = new FileInputStream(file);
		try {
			Document document = XmlFunc.getDocument(fis);
			// 根节点是coverage，获取coverage下的project下的节点，遍历所有的package节点
			Element root = document.getDocumentElement();
			NodeList rootlist = root.getChildNodes();
			for (int i = 0; i < rootlist.getLength(); i++) {
				Node projectnode = rootlist.item(i);
				if (projectnode instanceof Element) {
					if (PROJECT.equals(projectnode.getNodeName())) {
						getclover(projectnode, PACKAGE, FILES, project);
					}
				}
			}
		} finally {
			fis.close();
		}
		int coveredelements = 0;
		int total = 0;
		double result = 0.0;
		for (Iterator<CloverBean> iterator = list.iterator(); iterator.hasNext();) {
			CloverBean cloverbean = iterator.next();
			coveredelements += cloverbean.getCloverelements();
			total += cloverbean.getElements();
		}
		System.out.println(coveredelements + "\t" + total);
		result = (coveredelements * 100) / total;
		System.out.println(javacount);
		return result;
	}

	private static void getclover(Node node, String name, String attribute, String project) {
		NodeList nodelist = node.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node cnode = nodelist.item(i);
			if (cnode instanceof Element) {
				if (METRICS.equals(cnode.getNodeName())) {
					if (StrFunc.isNull(attribute)) {
						// attribute为空说明走到最后了，需要统计
						CloverBean cloverbean = new CloverBean(StrFunc.str2int(((Element) cnode).getAttribute(COVEREDELEMENTS), 0),
								StrFunc.str2int(((Element) cnode).getAttribute(STATEMENTS), 0), StrFunc.str2int(
										((Element) cnode).getAttribute(METHODS), 0), StrFunc.str2int(
										((Element) cnode).getAttribute(ELEMENTS), 0));
						FileFunc.writeStrToFile(RESULTPATH, cloverbean.toString(), true, StrFunc.UTF8);
						FileFunc.writeStrToFile(RESULTPATH, "\r\n", true, StrFunc.UTF8);
						list.add(cloverbean);
						break;
					} else {
						// 如果子节点为0，就不用继续遍历了
						if (0 == StrFunc.str2int(((Element) cnode).getAttribute(attribute), 0)) {
							break;
						}
					}
				} else {
					if (FILE.equals(cnode.getNodeName())) {
						if (((Element) cnode).getAttribute(PATH).startsWith(RESPONSEPATH + project)) {
							//							System.out.println(((Element) cnode).getAttribute("name"));
							javacount++;
							FileFunc.writeStrToFile(RESULTPATH, ((Element) cnode).getAttribute(PATH), true, StrFunc.UTF8);
							FileFunc.writeStrToFile(RESULTPATH, ",", true, StrFunc.UTF8);
							getclover(cnode, CLASS, null, project);
						} else {
							// 如果不是指定工程下的不统计
							break;
						}
					} else if (PACKAGE.equals(cnode.getNodeName())) {
						getclover(cnode, FILE, CLASSES, project);
					}
				}
			}
		}
	}
}

class CloverBean {
	private int coveredelements = 0;

	private int statements = 0;

	private int methods = 0;

	private int elements = 0;

	public CloverBean(int coveredelements, int statements, int methods, int elements) {
		super();
		this.coveredelements = coveredelements;
		this.statements = statements;
		this.methods = methods;
		this.elements = elements;
		System.out.println(this.toString());
	}

	public int getCloverelements() {
		return coveredelements;
	}

	public void setCoveredelements(int coveredelements) {
		this.coveredelements = coveredelements;
	}

	public int getStatements() {
		return statements;
	}

	public void setStatements(int statements) {
		this.statements = statements;
	}

	public int getMethods() {
		return methods;
	}

	public void setMethods(int methods) {
		this.methods = methods;
	}

	public int getElements() {
		return elements;
	}

	public void setElements(int elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		//		return "CloverBean [cloverelements=" + cloverelements + ", statements=" + statements + ", methods=" + methods
		//				+ ", elements=" + elements + "]";
		return coveredelements + "," + statements + "," + methods + "," + elements;
	}

}