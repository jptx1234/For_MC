package com.github.jptx1234.For;

import java.util.Arrays;
import java.util.HashMap;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class For extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		getServer().getLogger().info("for命令成功加载");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length == 0 || args[0].equals("h")) {
//			TODO 帮助信息
			sender.sendMessage("帮助信息");
			return true;
		}
		String[] loopVars = args[0].split(";");
		if (loopVars.length != 3) {
			showError(sender, args[0], "格式有误");
			return true;
		}
		
//		解析第一段
		String[] loopVars1 = loopVars[0].split("=");
		if (loopVars1.length != 2) {
			showError(sender, loopVars[0], "格式有误");
			return true;
		}
		String loopVarName = loopVars1[0];
		String loopVarStart;
		try {
			Double.parseDouble(loopVars1[1]);
		} catch (NumberFormatException e) {
			showError(sender, loopVars[0], loopVars1[1]+"不是可识别的数值");
			return true;
		}
		loopVarStart = loopVars1[1];
		
//		解析第二段
		String loopCondition;
		if (loopVars[1].length() == 0) {
			loopCondition = "";
		}else {
			Evaluator part2TestEval = new Evaluator();
			part2TestEval.putVariable(loopVarName, loopVarStart);
			loopCondition = getEvalFormat(loopVars[1], loopVarName);
			try {
				part2TestEval.getBooleanResult(loopCondition);
			} catch (EvaluationException e) {
				showError(sender, loopVars[1], "条件表达式有误");
				return true;
			}
		}
		
//		解析第三段
		String loopExpression;
		if (loopVars[2].length() == 0) {
			loopExpression = "";
		}else {
			String loopVars3 = loopVars[2];
			if (loopVars3.matches("("+loopVarName+"\\+\\+)|(\\+\\+"+loopVarName+")")) {
				loopVars3 = loopVarName+"+1";
			}else if (loopVars3.matches("("+loopVarName+"--)|(--"+loopVarName+")")) {
				loopVars3 = loopVarName+"-1";
			}
			Evaluator part3TestEval = new Evaluator();
			part3TestEval.putVariable(loopVarName, String.valueOf(loopVarStart));
			loopExpression = getEvalFormat(loopVars3, loopVarName);
			try {
				part3TestEval.evaluate(loopExpression);
			} catch (EvaluationException e) {
				showError(sender, loopVars[2], "表达式有误");
				return true;
			}
		}
		
//		解析循环体
		if (args.length == 1) {
			sender.sendMessage(ChatColor.RED+"被循环的命令不能为空");
			return true;
		}
		args[1].replaceAll("/", "");
		String[] cmd = Arrays.copyOfRange(args, 1, args.length);
		
		
//		解析完毕，开始操作
//		sender.sendMessage("loopVarName="+loopVarName+" loopStart="+loopVarStart+"  condition="+loopCondition+"  expression="+loopExpression+"  cmd="+cmd[0]);
		loop(sender, loopVarName, loopVarStart, loopCondition, loopExpression, cmd);
		
		return true;
	}
	
	public void showError(CommandSender sender,String errCode,String reason){
		sender.sendMessage(ChatColor.RED+"以下代码有错误： "+ChatColor.WHITE+errCode+ChatColor.RED+"\n出错原因： "+ChatColor.WHITE+reason+ChatColor.RED
				+"\n请检查出错语句或使用"+ChatColor.WHITE+" /for h "+ChatColor.RED+"查看帮助");
	}
	
	public void loop(CommandSender sender,String loopVarName,String loopStart,String condition,String expression,String[] cmd){
		Player player = (Player) sender;
		Evaluator evaluator = new Evaluator();
		evaluator.putVariable(loopVarName, loopStart);
		HashMap<String, Boolean> evalable = new HashMap<>(cmd.length);
		for (String string : cmd) {
			evalable.put(string, true);
		}
		try {
			while (condition.length() == 0 || evaluator.getBooleanResult(condition)) {
				StringBuilder cmdSB = new StringBuilder("/");
				for (String perCMD : cmd) {
					if (evalable.get(perCMD)) {
						try {
							perCMD = transformDoubleSymbol(perCMD, loopVarName);
							perCMD = evaluator.evaluate(getEvalFormat(perCMD, loopVarName));
						} catch (EvaluationException e) {
							evalable.put(perCMD, false);
						}
					}
					cmdSB.append(perCMD+" ");
				}
				if (expression.length() != 0) {
					String nextLoopValue = evaluator.evaluate(expression);
					evaluator.putVariable(loopVarName, nextLoopValue);
				}
				player.chat(cmdSB.toString());
			}
		} catch (EvaluationException e) {
			player.sendMessage("执行命令中出错");
		}
	}
	
	public static String getEvalFormat(String expression,String varName){
		return expression.replaceAll(varName, "#{"+varName+"}");
	}
	
	public static String transformDoubleSymbol(String expression,String varName){
		if (expression.matches("("+varName+"\\+\\+)|(\\+\\+"+varName+")")) {
			expression = varName+"+1";
		}else if (expression.matches("("+varName+"--)|(--"+varName+")")) {
			expression = varName+"-1";
		}
		return expression;
	}

}
