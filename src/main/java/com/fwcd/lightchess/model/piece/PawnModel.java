package com.fwcd.lightchess.model.piece;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import com.fwcd.lightchess.model.ChessBoardModel;
import com.fwcd.lightchess.model.ChessFieldModel;
import com.fwcd.lightchess.model.ChessMove;
import com.fwcd.lightchess.model.ChessPosition;
import com.fwcd.lightchess.model.PlayerColor;
import com.fwcd.lightchess.utils.Streams;

public class PawnModel extends AbstractPieceModel {
	private int moves = 0;
	
	public PawnModel(PlayerColor color, ChessPosition position) {
		super(color, position);
	}
	
	@Override
	public Stream<ChessMove> getPossibleMoves(ChessBoardModel board) {
		// TODO: Promotion
		Stream.Builder<ChessMove> moves = Stream.builder();
		ChessPosition origin = getPosition();
		
		stepsFrom(origin, board)
			.filter(it -> !board.fieldAt(it).hasPiece())
			.map(it -> new ChessMove(this, origin, it))
			.forEach(moves::add);
		diagonalStepsFrom(origin)
			.map(it -> {
				if (board.fieldAt(it).hasPieceOfColor(getColor().opponent())) {
					return Optional.of(new ChessMove(this, origin, it));
				} else if (isEnPassantPossible(origin, it, board)) {
					return Optional.of(new ChessMove(this, origin, it, getEnPassantCapturePos(it)));
				} else return Optional.<ChessMove>empty();
			})
			.filter(Optional::isPresent)
			.map(it -> it.orElseThrow(NoSuchElementException::new))
			.forEach(moves::add);
		
		return moves.build().distinct();
	}
	
	/** 
	 * Returns the y-index of a rank on which this
	 * pawn could capture an opposing pawn through "en passant"
	 */
	private int getEnPassantY() {
		switch (getColor()) {
			case WHITE: return 3;
			case BLACK: return 4;
			default: throw new IllegalStateException("Invalid pawn color");
		}
	}
	
	private int getStepY() {
		switch (getColor()) {
			case WHITE: return -1;
			case BLACK: return 1;
			default: throw new IllegalStateException("Invalid pawn color");
		}
	}
	
	private Optional<ChessPosition> getEnPassantCapturePos(ChessPosition destination) {
		return destination.up(getStepY());
	}
	
	private boolean isEnPassantPossible(ChessPosition origin, ChessPosition destination, ChessBoardModel board) {
		Optional<ChessPosition> capturedPos = getEnPassantCapturePos(destination);
		return origin.getY() == getEnPassantY()
			&& capturedPos
				.flatMap(it -> board.pieceAt(it))
				.filter(it -> it.canBeCapturedThroughEnPassant() && it.getColor().equals(getColor().opponent()))
				.isPresent();
	}
	
	private Stream<ChessPosition> stepsFrom(ChessPosition origin, ChessBoardModel board) {
		int step = getStepY();
		Optional<ChessPosition> firstStep = origin.down(step);
		
		// Only return two steps when this is the first move and
		// no piece is directly in front of this pawn
		if ((moves == 0) && !(firstStep.map(board::fieldAt).filter(ChessFieldModel::hasPiece).isPresent())) {
			return Streams.filterPresent(Stream.of(firstStep, origin.down(step * 2)));
		} else {
			return Streams.filterPresent(Stream.of(firstStep));
		}
	}
	
	private Stream<ChessPosition> diagonalStepsFrom(ChessPosition origin) {
		int stepY = getStepY();
		return Streams.filterPresent(Stream.of(
			origin.plus(-1, stepY),
			origin.plus( 1, stepY)
		));
	}
	
	@Override
	public void accept(ChessPieceVisitor visitor) {
		visitor.visitPawn(this);
	}
	
	@Override
	public boolean canBeCapturedThroughEnPassant() {
		return moves == 1;
	}
	
	@Override
	protected void onMove() {
		moves++;
	}
	
	@Override
	public boolean threatens(ChessPosition position, ChessBoardModel board) {
		return diagonalStepsFrom(getPosition())
				.anyMatch(it -> it.equals(position));
	}
	
	@Override
	public ChessPieceType getType() { return ChessPieceType.PAWN; }
	
	@Override
	public ChessPieceModel copy() { return new PawnModel(getColor(), getPosition()); }
}
