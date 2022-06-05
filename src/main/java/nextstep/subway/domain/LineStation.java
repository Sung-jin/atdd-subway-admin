package nextstep.subway.domain;

import javax.persistence.*;

@Entity
public class LineStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Distance distance = new Distance();

    @ManyToOne(fetch = FetchType.LAZY)
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Station downStation;

    private boolean isStart;

    private boolean isLast;

    protected LineStation() {}

    public LineStation(Distance distance, Station upStation, Station downStation, boolean isFirstAdd) {
        validation(upStation, downStation);

        this.distance = distance;
        this.upStation = upStation;
        this.downStation = downStation;

        if (isFirstAdd) {
            this.isStart = true;
            this.isLast = true;
        }
    }

    public boolean isSameUpStation(Station upStation) {
        return this.upStation.isSameId(upStation.getId());
    }

    public boolean isAddNewFirst(Station downStation) {
        return this.isStart && this.upStation.isSameId(downStation.getId());
    }

    public boolean isSameDownStation(Station downStation) {
        return this.downStation.isSameId(downStation.getId());
    }

    public boolean isAddNewLast(Station upStation) {
        return this.isLast && this.downStation.isSameId(upStation.getId());
    }

    public LineStation addStation(Station upStation, Station downStation, Distance distance) {
        LineStation addResult = new LineStation(distance, upStation, downStation, false);

        if (isAddNewFirst(downStation)) {
            return addNewLineFirstStation(addResult);
        }
        if (isAddNewLast(upStation)) {
            return addNewLineLastStation(addResult);
        }
        if (isAddUpToMiddle(upStation.getId())) {
            return addUpToMiddle(addResult);
        }
        if (isAddMiddleToDown(downStation.getId())) {
            return addMiddleToDown(addResult);
        }

        throw new IllegalArgumentException("지하철을 추가할 수 없습니다.");
    }

    private LineStation addNewLineFirstStation(LineStation addResult) {
        this.isStart = false;
        addResult.isStart = true;

        return addResult;
    }

    private LineStation addNewLineLastStation(LineStation addResult) {
        this.isLast = false;
        addResult.isLast = true;

        return addResult;
    }

    private LineStation addUpToMiddle(LineStation addResult) {
        checkPossibleAddSection(addResult.getDistance());

        addResult.isStart = this.isStart;
        this.isStart = false;
        this.upStation = addResult.downStation;
        this.distance.subtract(addResult.distance);

        return addResult;
    }

    private LineStation addMiddleToDown(LineStation addResult) {
        checkPossibleAddSection(addResult.getDistance());

        addResult.isLast = this.isLast;
        this.isLast = false;
        this.downStation = addResult.upStation;
        this.distance.subtract(addResult.distance);

        return addResult;
    }

    private boolean isAddUpToMiddle(Long upStationId) {
        return this.upStation.isSameId(upStationId);
    }

    private boolean isAddMiddleToDown(Long downStationId) {
        return this.downStation.isSameId(downStationId);
    }

    private void validation(Station upStation, Station downStation) {
        // spring validator 로 대체 가능
        if (upStation == null) {
            throw new IllegalArgumentException("상행역 정보는 필수입니다.");
        }
        if (downStation == null) {
            throw new IllegalArgumentException("하행역 정보는 필수입니다.");
        }
    }

    private void checkPossibleAddSection(Distance distance) {
        if (this.distance.isLessThenOrSame(distance)) {
            throw new IllegalArgumentException("기존 노선의 길이와 같거나 긴 노선을 추가할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Distance getDistance() {
        return distance;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isLast() {
        return isLast;
    }
}