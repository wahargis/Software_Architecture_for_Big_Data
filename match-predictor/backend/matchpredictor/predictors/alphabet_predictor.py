from matchpredictor.matchresults.result import Fixture, Outcome, Result, Team
from matchpredictor.predictors.predictor import Predictor, Prediction


class AlphabetPredictor(Predictor):
    def predict(self, fixture: Fixture) -> Prediction:
        lower_home_name = str(fixture.home_team).lower()
        lower_away_name = str(fixture.away_team).lower()

        if lower_home_name is None:
            return Prediction(outcome=Outcome.AWAY)
        if lower_away_name is None:
            return Prediction(outcome=Outcome.HOME)

        if lower_home_name > lower_away_name:
            return Prediction(outcome=Outcome.HOME)
        elif lower_home_name < lower_away_name:
            return Prediction(outcome=Outcome.AWAY)
        else:
            return Prediction(outcome=Outcome.DRAW)