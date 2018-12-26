package org.mule.connector.justride.internal;

import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.http.api.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class JustrideConnectionProvider implements CachedConnectionProvider<JustrideConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(JustrideConnectionProvider.class);

 /**
  * A parameter that is always required to be configured.
  */
  @Parameter
  @DisplayName("ACCESS TOKEN")
  private String token;

 /**
  * A parameter that is not required to be configured by the user.
  */
  @DisplayName("API URL")
  @Parameter
  @Optional(defaultValue = "api/v1")
  private String apiUri;

  @Inject
  HttpService httpService;
  
  @Override
  public JustrideConnection connect() throws ConnectionException {
    return new JustrideConnection(token , apiUri, httpService);
  }

  @Override
  public void disconnect(JustrideConnection connection) {
      connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(JustrideConnection connection) {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      Reference<ConnectionValidationResult> result = new Reference<>();
      connection.auth.test(apiUri).whenCompleteAsync((httpResponse, throwable) -> {
          if(throwable != null){
              result.set(failure("Unable to connect to Slack." + throwable.getMessage(), new ConnectionException(throwable)));
              countDownLatch.countDown();
              return;
          }

          String response = IOUtils.toString(httpResponse.getEntity().getContent());
          /*Map<String, Object> javaResponse = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
          }.getType());

          Boolean isOk = (Boolean) javaResponse.get("ok");*/
          if(response != null){
              result.set(success());
          } else {
             
              result.set(failure("Unable to connect to Justride ", new ConnectionException("Unable to connect to Target System.")));
          }
          countDownLatch.countDown();
      });
      try {
          countDownLatch.await();
      } catch (InterruptedException e) {
          return failure(e.getMessage(), e);
      }
      return result.get();
  }
}
