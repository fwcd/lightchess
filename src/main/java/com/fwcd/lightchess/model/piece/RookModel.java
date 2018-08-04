package com.fwcd.lightchess.model.piece;

import java.util.ArrayList;
import java.util.List;

import com.fwcd.lightchess.model.ChessBoardModel;
import com.fwcd.lightchess.model.ChessPosition;
import com.fwcd.lightchess.model.PlayerColor;

public class RookModel implements ChessPieceModel {
	private final PlayerColor color;
	
	public RookModel(PlayerColor color) {
		this.color = color;
	}
	
	@Override
	public List<ChessPosition> getPossibleMoves(ChessPosition pos, ChessBoardModel board) {
		List<ChessPosition> targets = new ArrayList<>();
		// Horizontals
		PieceUtils.addPositionsUntilHit(1, 0, pos, targets, board);
		PieceUtils.addPositionsUntilHit(-1, 0, pos, targets, board);
		// Verticals
		PieceUtils.addPositionsUntilHit(0, 1, pos, targets, board);
		PieceUtils.addPositionsUntilHit(0, -1, pos, targets, board);
		return targets;
	}
	
	@Override
	public PlayerColor getColor() { return color; }
	
	@Override
	public void accept(ChessPieceVisitor visitor) {
		visitor.visitRook(this);
	}
}
